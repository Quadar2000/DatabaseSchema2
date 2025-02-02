"use client";

import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { useRouter } from 'next/navigation';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState("");
  const [roles, setRoles] = useState("");
  const [id, setId] = useState("");
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  const fetchSession = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/auth/session-info',
        {
          headers: {
            'Content-Type': 'application/json',
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }
      ); // Endpoint sprawdzający sesję
      setUser(response.data.user);
      setRoles(response.data.roles);
      setId(response.data.id);
    } catch (error) {
      if (error.response?.status === 401) {
        // Jeśli sesja wygasła, przekieruj do strony logowania
        setUser("");
        setRoles("");
        setId("");
        router.push({
          pathname: '/',
          query: { message: 'Session expired, you have been logged out' },
        });
      } else {
        console.error('Error while checking session:', error.response?.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const refreshUser = async () => {
    setLoading(true); 
    await fetchSession(); 
    setLoading(false); 
  }

  const logOut = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/auth/logout',
        {
          headers: {
            'Content-Type': 'application/json',
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }
      ); // Endpoint sprawdzający sesję
    } catch (error) {
        console.error('Error while logging out:', error);
    }
    setLoading(true); 
    await fetchSession(); 
    setLoading(false); 
  }
  

  // Sprawdzanie sesji co określony czas
  useEffect(() => {
    // Wywołanie fetchSession natychmiast po zalogowaniu
    fetchSession();
    
    const intervalId = setInterval(fetchSession, 1.1 * 60 * 1000);

    // Wyczyszczenie interwału po opuszczeniu komponentu
    return () => clearInterval(intervalId);
  }, [router]);

  return (
    <AuthContext.Provider value={{ user, id, roles, loading, refreshUser,logOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);