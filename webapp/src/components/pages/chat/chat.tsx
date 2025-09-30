// src/components/ChatButton.tsx
import React, { useState } from "react";
import "./chat.css";

interface ChatButtonProps {
  /**
   * Optional prop for the button text.
   */
  buttonText?: string;
}

const ChatButton: React.FC<ChatButtonProps> = ({ buttonText = "AuChat" }) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="chat-container">
      {!isOpen && (
        <button className="chat-toggle-button" onClick={toggleChat}>
          {buttonText}
        </button>
      )}

      {isOpen && (
        <div className="chat-window">
          <div className="chat-header">
            <h3>Support Chat</h3>
            <button className="chat-close-button" onClick={toggleChat}>
              &times;
            </button>
          </div>
          <div className="chat-body">
            {/* Your chat UI goes here */}
            <p>Hello! How can we help you today?</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatButton;
