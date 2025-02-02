"use client";

import { useState,useEffect } from 'react';
import axios from 'axios';
import StyledDiv from '../StyledDiv/StyledDiv';
import StyledButton from '../StyledButton/StyledButton';
import { getCsrfToken } from '@/app/functions/getCsrfToken/getCsrfToken';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/app/AuthProvider';

export default function SignIn() {

  const [error, setError] = useState('');
  const [csrfToken, setCsrfToken] = useState('');
  const router = useRouter();
  const { refreshUser } = useAuth();
  //const [message, setMessage] = useState('');
  const { message } = router.query;
  

  const handleSubmit = async (event) => {
    event.preventDefault();
    const email = event.target.email.value;
    const password = event.target.password.value;
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

    // useEffect(() => {
    //   if (router.state?.message) {
    //     setMessage(router.state.message);
    //   }
    // }, [router.state]);

  return (
    <StyledDiv>
    <form onSubmit={handleSubmit}>
      {/* <input name="csrfToken" type="hidden" defaultValue={csrfToken} /> */}
         <label>Email</label>
         <br />
         <input id="email" name="email" type="text" required />
         <br />
         <br />
         <label>Password</label>
         <br />
         <input id="password" name="password" type="password" required />
         <br />
         <br />
         <StyledButton type="submit">Sign in</StyledButton>
         <br />
         <br />
        
    </form>
    {error && <p style={{ color: "red" }}>{error}</p>}
    {message && <p style={{ color: 'green' }}>{message}</p>}
    </StyledDiv>
    
  );
}


