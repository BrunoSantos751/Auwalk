import React, { createContext, useState, useEffect, ReactNode } from "react";
import * as jwt from "jwt-decode";

interface AuthContextType {
  isLoggedIn: boolean;
  userEmail: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
  isLoggedIn: false,
  userEmail: null,
  login: () => {},
  logout: () => {},
});

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);

  const updateFromStorage = () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setIsLoggedIn(false);
      setUserEmail(null);
      return;
    }

    try {
      const decoded: any = (jwt as any).default(token);
      const exp = decoded?.exp;
      const email = decoded?.sub || decoded?.email || null;
      const currentTime = Date.now() / 1000;

      if (exp && exp < currentTime) {
        localStorage.removeItem("token");
        setIsLoggedIn(false);
        setUserEmail(null);
      } else {
        setIsLoggedIn(true);
        setUserEmail(email);
      }
    } catch {
      localStorage.removeItem("token");
      setIsLoggedIn(false);
      setUserEmail(null);
    }
  };

  useEffect(() => {
    updateFromStorage();
  }, []);

  const login = (token: string) => {
    localStorage.setItem("token", token);
    updateFromStorage();
  };

  const logout = () => {
    localStorage.removeItem("token");
    updateFromStorage();
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, userEmail, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
