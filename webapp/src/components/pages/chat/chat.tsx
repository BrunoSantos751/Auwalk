import React, { useState, useEffect, useRef } from "react";
import "./chat.css";

// Interfaces para tipar os dados
interface Chat {
    id_chat: number;
    nome_contraparte: string;
}
interface Message {
    id_mensagem: number; // Corrigido para corresponder ao backend
    id_chat: number;
    id_remetente: number;
    conteudo: string;
    data_envio: string;
}

const ChatButton: React.FC = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [view, setView] = useState<'list' | 'messages'>('list');
    const [chats, setChats] = useState<Chat[]>([]);
    const [activeChat, setActiveChat] = useState<Chat | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [newMessage, setNewMessage] = useState("");
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const token = localStorage.getItem("authToken");
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split(".")[1]));
                setCurrentUserId(parseInt(payload.sub, 10));
            } catch (error) {
                console.error("Falha ao decodificar o token:", error);
            }
        }
    }, []);

    useEffect(() => {
        if (isOpen && view === 'list') {
            const fetchChats = async () => {
                const token = localStorage.getItem("authToken");
                if (!token) return;
                try {
                    const response = await fetch('http://localhost:8080/chats', {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });
                    const result = await response.json();
                    if (result.sucesso) setChats(result.dados);
                } catch (error) { console.error("Erro ao buscar chats:", error); }
            };
            fetchChats();
        }
    }, [isOpen, view]);

    useEffect(() => {
        if (activeChat) {
            const fetchMessages = async () => {
                const token = localStorage.getItem("authToken");
                if (!token) return;
                try {
                    const response = await fetch(`http://localhost:8080/mensagens/${activeChat.id_chat}`, {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });
                    const result = await response.json();
                    setMessages(Array.isArray(result) ? result : []);
                } catch (error) { console.error("Erro ao buscar mensagens:", error); }
            };
            fetchMessages();
        }
    }, [activeChat]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const toggleChat = () => setIsOpen(!isOpen);

    const handleSelectChat = (chat: Chat) => {
        setActiveChat(chat);
        setView('messages');
    };

    const handleBackToList = () => {
        setActiveChat(null);
        setMessages([]);
        setView('list');
    };

    const handleSendMessage = async () => {
        if (!newMessage.trim() || !activeChat || !currentUserId) return;
        const token = localStorage.getItem("authToken");

        const payload = {
            id_chat: activeChat.id_chat,
            conteudo: newMessage
        };

        try {
            const response = await fetch('http://localhost:8080/mensagens', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const novaMensagem = await response.json();
                setMessages(prev => [...prev, novaMensagem]);
                setNewMessage("");
            }
        } catch (error) { console.error("Erro ao enviar mensagem:", error); }
    };

    return (
        <div className="chat-container">
            {!isOpen && <button className="chat-toggle-button" onClick={toggleChat}>AuChat</button>}

            {isOpen && (
                <div className="chat-window">
                    <div className="chat-header">
                        {view === 'messages' && <button className="back-button" onClick={handleBackToList}>&#8592;</button>}
                        <h3>{view === 'list' ? 'Minhas Conversas' : activeChat?.nome_contraparte}</h3>
                        <button className="chat-close-button" onClick={toggleChat}>&times;</button>
                    </div>

                    {view === 'list' && (
                        <div className="chat-body">
                            {chats.length > 0 ? chats.map(chat => (
                                <div key={chat.id_chat} className="chat-list-item" onClick={() => handleSelectChat(chat)}>
                                    <p>{chat.nome_contraparte}</p>
                                </div>
                            )) : <p style={{padding: '15px'}}>Nenhuma conversa iniciada.</p>}
                        </div>
                    )}

                    {view === 'messages' && activeChat && (
                        <div className="messages-view">
                            <div className="messages-container">
                                {messages.map(msg => (
                                    <div key={msg.id_mensagem} className={`message ${msg.id_remetente === currentUserId ? 'sent' : 'received'}`}>
                                        {msg.conteudo}
                                    </div>
                                ))}
                                <div ref={messagesEndRef} />
                            </div>
                            <div className="chat-footer">
                                <input
                                    type="text"
                                    className="chat-input"
                                    placeholder="Digite uma mensagem..."
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                />
                                <button className="send-button" onClick={handleSendMessage}>&#10148;</button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default ChatButton;
