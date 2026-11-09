package com.example.contract.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class QuestionAnswerService {

    private static final String NOT_FOUND_MESSAGE = "Information not found in the contract.";

    // Common English stop words to filter out when scoring questions
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "can", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for",
            "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself",
            "him", "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just",
            "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once",
            "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "same", "she",
            "should", "so", "some", "such", "than", "that", "the", "their", "theirs", "them", "themselves",
            "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom",
            "why", "with", "would", "you", "your", "yours", "yourself", "yourselves"
    ));

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    /**
     * Answers a question based on extracted contract text using sentence splitting,
     * keyword tokenization, stop-word removal, and keyword overlap scoring.
     */
    public String answerQuestion(String contractText, String question) {
        if (contractText == null || contractText.isBlank() || question == null || question.isBlank()) {
            return NOT_FOUND_MESSAGE;
        }

        // 1. Extract keywords from question
        List<String> questionKeywords = extractKeywords(question);
        if (questionKeywords.isEmpty()) {
            return NOT_FOUND_MESSAGE;
        }

        // 2. Split contract text into sentences
        List<String> sentences = splitIntoSentences(contractText);
        if (sentences.isEmpty()) {
            return NOT_FOUND_MESSAGE;
        }

        // 3. Score each sentence based on keyword occurrences
        String bestSentence = null;
        int maxScore = 0;

        for (String sentence : sentences) {
            int score = scoreSentence(sentence, questionKeywords);
            if (score > maxScore) {
                maxScore = score;
                bestSentence = sentence;
            }
        }

        // 4. Return the best sentence if score > 0, otherwise return default not found message
        if (maxScore > 0 && bestSentence != null) {
            return bestSentence.trim();
        }

        return NOT_FOUND_MESSAGE;
    }

    /**
     * Splits plain text into individual sentences using punctuation and newline boundaries.
     */
    public List<String> splitIntoSentences(String text) {
        if (text == null) {
            return Collections.emptyList();
        }

        // Normalize Windows and Unix line breaks
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");

        // Split by standard sentence terminators (. ? !) or consecutive newlines
        String[] rawSplits = normalized.split("(?<=[.?!])\\s+|\\n{2,}");
        List<String> result = new ArrayList<>();

        for (String s : rawSplits) {
            // Also split remaining single newline bullet-like items if present
            String[] lines = s.split("\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }

        return result;
    }

    /**
     * Tokenizes input text into lowercase alphanumeric keywords, filtering out stop words.
     */
    public List<String> extractKeywords(String text) {
        if (text == null) {
            return Collections.emptyList();
        }

        String[] tokens = WORD_PATTERN.split(text.toLowerCase(Locale.ENGLISH));
        List<String> keywords = new ArrayList<>();

        for (String token : tokens) {
            token = token.trim();
            if (token.length() > 1 && !STOP_WORDS.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords;
    }

    /**
     * Calculates the matching score of a candidate sentence against the question keywords.
     */
    private int scoreSentence(String sentence, List<String> questionKeywords) {
        String lowerSentence = sentence.toLowerCase(Locale.ENGLISH);
        Set<String> sentenceTokens = new HashSet<>(Arrays.asList(WORD_PATTERN.split(lowerSentence)));

        int score = 0;
        for (String keyword : questionKeywords) {
            if (sentenceTokens.contains(keyword)) {
                score += 2; // exact word match
            } else if (lowerSentence.contains(keyword)) {
                score += 1; // substring match
            }
        }

        return score;
    }
}

