"use client";

import IsLoggedIn from '@/app/components/IsLoggedIn/IsLoggedIn';
import { useRouter } from 'next/navigation';
import styles from "./profile.module.css";
import StyledButton from '../components/StyledButton/StyledButton';
import StyledDiv from '../components/StyledDiv/StyledDiv';
import { useAuth } from '../AuthProvider';
import { useEffect, useState } from 'react';
import { getCsrfToken } from "@/app/functions/getCsrfToken/getCsrfToken";
import axios from 'axios';

const Profile = () => {
    const {id} = useAuth();
    const router = useRouter();

    const [userData, setUser] = useState({});
    const [error, setError] = useState("");
    const [csrfToken, setCsrfToken] = useState('');

    useEffect(() => {
        const getUser = async () => {
            try {
                const response = await axios.get(
                  `http://localhost:8080/api/get-profile`,
                  
                  {
                    params:{
                      id: id
                    },
                    headers: {
                      'Content-Type': 'application/json',
                    },
                    withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
                  }
            
                
                );
            
                const data = response.data;
                setUser(data.user);
              } catch (error) {
                if (error.response?.status === 404){
                  setError(error.response?.data.message || "Something went wrong");
                } else {
                  setError(error.message);
                }
                
              }
        }
        getUser();
        const fetchCsrfToken = async () => {
            const token = await getCsrfToken();
            setCsrfToken(token);
          };
          fetchCsrfToken();
      }, [])


    const handleChangePasswordClick = () => { 
        router.push('/profile/change-password'); 
      }
    return(
        <IsLoggedIn>
          <div style={{display: 'flex',justifyContent: 'center'}}>
            <StyledDiv style={{marginTop: '200px',border: '1px solid #ccc'}}>
                <h1>Username</h1>
                <p>{userData.name}</p>
                <h1>Email</h1>
                <p>{userData.email}</p>
                <StyledButton onClick = {handleChangePasswordClick}>Change Password</StyledButton>
                {error && <p style={{ color: "red" }}>{error}</p>}
            </StyledDiv>
          </div>
            
        </IsLoggedIn>
    
    );
};


export default Profile;
