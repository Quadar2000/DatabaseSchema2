"use client";

import IsLoggedIn from '@/app/components/IsLoggedIn/IsLoggedIn';
import StyledButton from '@/app/components/StyledButton/StyledButton';
import {DivInForm, StyledDiv} from '@/app/components/StyledDiv/StyledDiv';
import {StyledForm,StyledInput }from '@/app/components/StyledForm/StyledForm';
import { useEffect, useState } from 'react';
import { getCsrfToken } from '@/app/functions/getCsrfToken/getCsrfToken';
import axios from 'axios';
import { useAuth } from '@/app/AuthProvider';

const ChangePassword = () => {
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [csrfToken, setCsrfToken] = useState('');

  useEffect(() => {
    const fetchCsrfToken = async () => {
      const token = await getCsrfToken();
      setCsrfToken(token);
    };
    fetchCsrfToken();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setError("");
    setSuccess("");

    try {
      const response = await axios.post(
        `http://localhost:8080/api/change-password`,
        {
          newPassword: newPassword,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken,  
          },
          withCredentials: true,  
        }
      );

      const data = response.data;
      setSuccess(data.message);
    } catch (error) {
      if (error.response?.status === 400 || error.response?.status === 500){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }
  };

  return (
    <IsLoggedIn>
      <StyledDiv className='black' style={{height: '600px'}}>
        <StyledForm onSubmit={handleSubmit}>
          <input type="hidden" name="csrfToken" value={csrfToken} />
          <DivInForm>
            <label>New Password</label>
            <StyledInput
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
          </DivInForm>

          <DivInForm>
            <label>Confirm Password</label>
            <StyledInput
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </DivInForm>

          

          <StyledButton type="submit">Change Password</StyledButton>
        </StyledForm>
        {error && <p style={{ color: "red" }}>{error}</p>}
        {success && <p style={{ color: "green" }}>{success}</p>}
      </StyledDiv>
    </IsLoggedIn>
  );
}


export default ChangePassword;