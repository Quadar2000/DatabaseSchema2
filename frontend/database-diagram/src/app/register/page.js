"use client";

import WithRole from "../components/WithRole/WithRole";
import { useEffect, useState } from 'react';
import {StyledDiv, DivInForm} from "../components/StyledDiv/StyledDiv";
import {StyledForm,StyledInput} from "../components/StyledForm/StyledForm";
import StyledButton from "../components/StyledButton/StyledButton";
import { getCsrfToken } from "../functions/getCsrfToken/getCsrfToken";
import axios from "axios";

// const formReducer = (state, action) => {
//     return {
//       ...state,
//       [action.name]: action.value
//     };
//   };

const Register = () => {
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [csrfToken, setCsrfToken] = useState('');
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");


  useEffect(() => {
    const fetchCsrfToken = async () => {
      const token = await getCsrfToken();
      setCsrfToken(token);
    };
    fetchCsrfToken();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setError("");
    setSuccess("");

    try {
      const response = await axios.post(
        'http://localhost:8080/api/register-user',
        {
          name,
          email,
          password,
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
      if (error.response?.status === 400){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }
  };


  return (
    <WithRole role="ROLE_ADMIN">
      <StyledDiv className='black' style={{height: '600px'}}>
        <h1>Create New User</h1>
        <StyledForm onSubmit={handleSubmit}>
          <DivInForm>
            <label>Username</label>
            <StyledInput
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </DivInForm>

          <DivInForm>
            <label>Email</label>
            <StyledInput
              type="email"
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

          <DivInForm>
            <label>Confirm Password</label>
            <StyledInput
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </DivInForm>

          

          <StyledButton type="submit">Create User</StyledButton>
        </StyledForm>
        {error && <p style={{ color: "red" }}>{error}</p>}
        {success && <p style={{ color: "green" }}>{success}</p>}
      </StyledDiv>
    </WithRole>
    
    
  );
}


export default Register;