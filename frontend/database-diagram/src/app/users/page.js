"use client"

import { useEffect, useState } from "react";
import WithRole from "../components/WithRole/WithRole";
import { useRouter } from 'next/navigation';
import StyledButton from "../components/StyledButton/StyledButton";
import ConfirmModal from "../components/ConfirmModal/ConfirmModal";
import { getCsrfToken } from "../functions/getCsrfToken/getCsrfToken";
import StyledListItem from "../components/StyledListItem/StyledListItem";
import axios from "axios";
import StyledDiv from "../components/StyledDiv/StyledDiv";
import Spinner from "../components/Spinner/Spinner";

const UsersList = () => {
  
  const router = useRouter();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [csrfToken, setCsrfToken] = useState('');

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await axios.get(
        'http://localhost:8080/api/get-users',
        {
          headers: {
            'Content-Type': 'application/json', 
          },
          withCredentials: true,  
        }
  
      
      );
  
      const data = response.data;
      setUsers(data.users);
    } catch (error) {
      if (error.response?.status === 500){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }finally {
      setLoading(false);
    }
  };

  

  useEffect(() => {
    fetchUsers();
    const fetchCsrfToken = async () => {
      const token = await getCsrfToken();
      setCsrfToken(token);
    };
    fetchCsrfToken();
  }, []);


  const deleteUser = async (user) => {
    setSuccess("");
    setError("");
    try {
      const response = await axios.delete(
        `http://localhost:8080/api/delete-user`,
        {
          params:{
            id: user.id
          },
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken,  // Jeśli korzystasz z JWT
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }
  
      
      );
  
      const data = response.data;
      setSuccess(data.message);
      fetchUsers();
    } catch (error) {
      if (error.response?.status === 404){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }
  };

  const openModal = (user) => {
    setUserToDelete(user);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setUserToDelete(null);
  };

  const confirmDelete = async () => {
    closeModal();
    await deleteUser(userToDelete);
  };

  const goToUserDetail = async (id) => {
    try {
      const response = await axios.post(
        `http://localhost:8080/api/generate-token`,
        {
          userId: id, 
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken,
          },
          
          withCredentials: true, 
          
        },
        
      );

      const data = response.data;
      const token = data.token;
      router.push(`/users/permissions?id=${id}&token=${token}`);
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
      {loading ? 
      <Spinner/> :
      <StyledDiv>
        <h1>List of users</h1>
        {users.length === 0 ? <p>There is no user in database</p>:
        <ul>
        <StyledListItem>
          <div className="column">Name</div>
          <div className="column">Email</div>
          <div className="actions"></div>
        </StyledListItem> 
          {users.map((user) => (
            <StyledListItem key={user.email}>
              <div className="column">{user.name}</div>
              <div className="column">{user.email}</div>
              <div className="actions">
                <StyledButton onClick={() => goToUserDetail(user.id)}>
                  See details
                </StyledButton>
                <StyledButton onClick={() => openModal(user)}>
                  Delete User
                </StyledButton>
              </div>
            </StyledListItem>
          ))}
        </ul>}
        {isModalOpen && (
          <ConfirmModal
            message="Are you sure you want to remove this user?"
            onConfirm={confirmDelete}
            onCancel={closeModal}
          />
        )}
        {error && <p style={{ color: "red" }}>{error}</p>}
      </StyledDiv>}
    </WithRole>
    
    
  );
};


export default UsersList
