package handlers

import (
	"bytes"
	"encoding/json"
	"io"
	"net/http"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
)

type GeminiRequestPayload struct {
	Contents          []Content         `json:"contents"`
	SystemInstruction SystemInstruction `json:"system_instruction,omitempty"`
}

type Content struct {
	Role  string `json:"role"`
	Parts []Part `json:"parts"`
}

type Part struct {
	Text string `json:"text"`
}

type SystemInstruction struct {
	Parts []Part `json:"parts"`
}

func GeminiHandler(c *gin.Context) {
	apiKey := os.Getenv("GEMINI_API_KEY")
	if apiKey == "" {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "GEMINI_API_KEY environment variable not set"})
		return
	}
	url := "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey

	var clientRequest struct {
		Contents []Content `json:"contents"`
	}

	if err := c.ShouldBindJSON(&clientRequest); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body: " + err.Error()})
		return
	}

	var fullTextBuilder strings.Builder
	for _, content := range clientRequest.Contents {
		if len(content.Parts) > 0 {
			fullTextBuilder.WriteString(content.Parts[0].Text)
		}
	}
	fullText := fullTextBuilder.String()

	var historyBuilder strings.Builder
	parts := strings.Split(fullText, "/ai{")

	if len(parts) > 0 {
		userPart := strings.TrimSpace(parts[0])
		if userPart != "" {
			historyBuilder.WriteString("User: " + userPart + "\n")
		}
	}

	for i := 1; i < len(parts); i++ {
		subParts := strings.SplitN(parts[i], "}", 2)

		if len(subParts) > 0 {
			aiPart := strings.TrimSpace(subParts[0])
			if aiPart != "" {
				historyBuilder.WriteString("AI: " + aiPart + "\n")
			}
		}

		if len(subParts) > 1 {
			userPart := strings.TrimSpace(subParts[1])
			if userPart != "" {
				historyBuilder.WriteString("User: " + userPart + "\n")
			}
		}
	}

	processedContents := []Content{
		{
			Role: "user",
			Parts: []Part{
				{Text: historyBuilder.String()},
			},
		},
	}

	systemInstructionText := `You are a compassionate and insightful journaling assistant. Your goal is to help me explore my feelings and thoughts through Socratic questioning. The journal history is a conversation between 'User' and 'AI'. Your role is to provide the next 'AI' response.

Keep your response short. First, validate what I've said, then ask a gentle, open-ended question to help me delve deeper. Do not use markdown. Do not give direct advice, opinions, or solutions.

**Critical Error Handling Rule:**
If my *latest* message seems nonsensical, unintelligible, or just random characters (e.g., 'fjdksla;' or '???'), you MUST respond ONLY with: [Error: I'm having a little trouble understanding. Could you please try rephrasing that?]. Make sure all error responses end and start with box brackets.`

	geminiPayload := GeminiRequestPayload{
		Contents: processedContents,
		SystemInstruction: SystemInstruction{
			Parts: []Part{
				{Text: systemInstructionText},
			},
		},
	}

	payloadBytes, err := json.Marshal(geminiPayload)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create request payload"})
		return
	}

	req, err := http.NewRequest("POST", url, bytes.NewReader(payloadBytes))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create HTTP request"})
		return
	}
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to contact Gemini API"})
		return
	}
	defer resp.Body.Close()

	responseBody, err := io.ReadAll(resp.Body)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to read response from Gemini API"})
		return
	}

	c.Data(resp.StatusCode, "application/json; charset=utf-8", responseBody)
}
