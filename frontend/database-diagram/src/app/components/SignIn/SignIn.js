"use client";

import { useState,useEffect } from 'react';
import axios from 'axios';
import {StyledDiv, DivInForm} from '../StyledDiv/StyledDiv';
import StyledButton from '../StyledButton/StyledButton';
import { getCsrfToken } from '@/app/functions/getCsrfToken/getCsrfToken';
import {StyledForm, StyledInput} from '@/app/components/StyledForm/StyledForm';
import { useRouter,useSearchParams } from 'next/navigation';
import { useAuth } from '@/app/AuthProvider';

export default function SignIn() {

  const [error, setError] = useState('');
  const [csrfToken, setCsrfToken] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const router = useRouter();
  const { refreshUser } = useAuth();
  const searchParams = useSearchParams();
  const message = searchParams.get('message');
  

  const handleSubmit = async (event) => {
    event.preventDefault();
    const token = await getCsrfToken();
    setCsrfToken(token);

    try {
      const response = await axios.post(
        'http://localhost:8080/api/auth/login',
        {
          username: email,  // Zmienna z Reacta
          password: password,  // Zmienna z Reacta
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken, 
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }

      
      );

      await refreshUser();
      router.push('/');
    } catch (error) {
      if (error.response?.status === 401){
        setError('Wrong username or password');
      } else {
        setError(error.message);
      }
      
    }
  };

  useEffect(() => {
      const fetchCsrfToken = async () => {
        const token = await getCsrfToken();
        setCsrfToken(token);
      };
      fetchCsrfToken();
    }, []);


  return (
    <StyledDiv className='black'>
      <StyledForm onSubmit={handleSubmit}>

        <DivInForm>
          <label>Email</label>
          <StyledInput 
            type="text" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required 
          />
        </DivInForm>

        <DivInForm>
          <label>Password</label>
          <StyledInput 
            type="password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required 
          />
        </DivInForm>

        <StyledButton type = "submit">Sign in</StyledButton>
    </StyledForm>
    {error && <p style={{ color: "red" }}>{error}</p>}
    {message && <p style={{ color: 'green' }}>{message}</p>}
    </StyledDiv>
    
  );
}


