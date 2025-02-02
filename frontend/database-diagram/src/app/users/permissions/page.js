"use client"

import WithRole from '@/app/components/WithRole/WithRole';
import { useRouter,useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { getCsrfToken } from "@/app/functions/getCsrfToken/getCsrfToken";
import StyledButton from '@/app/components/StyledButton/StyledButton';
import ConfirmModal from '@/app/components/ConfirmModal/ConfirmModal';
import StyledForm from '@/app/components/StyledForm/StyledForm';
import StyledDiv from '@/app/components/StyledDiv/StyledDiv';
import StyledListItem from '@/app/components/StyledListItem/StyledListItem';
import axios from 'axios';
import Spinner from '@/app/components/Spinner/Spinner';

const Permissions = () => {
  const [isValid, setIsValid] = useState(false);
  const [permissions, setPermissionsValue] = useState([]);
  const [user, setUser] = useState({});
  const [error, setError] = useState("");
  const [permissionsError, setPermissionsError] = useState("");
  const [success, setSuccess] = useState("");
  const [dbUser, setDbUser] = useState("");
  const [host, setHost] = useState("");
  const [database, setDatabase] = useState("");
  const [password, setPassword] = useState("");
  const [port, setPort] = useState("");
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [permissionToDelete, setPermissionToDelete] = useState(null);
  const [csrfToken, setCsrfToken] = useState('');

  const router = useRouter();
  const searchParams = useSearchParams();
  const token  = searchParams.get('token');
  const id  = searchParams.get('id');

  if(!token || !id){
    router.replace('/');
  }

  const fetchPermissions = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/get-permissions`,
        
        {
          params:{
            userId: id
          },
          headers: {
            'Content-Type': 'application/json',
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }
  
      
      );
  
      const data = response.data;
      setPermissionsValue(data.permissions);
      setUser(data.user);
    } catch (error) {
      if (error.response?.status === 400){
        setPermissionsError(error.response?.data.message || "Something went wrong");
      } else {
        setPermissionsError(error.message);
      }
      
    }
  };


  useEffect(() => {
    fetchPermissions();
    const fetchCsrfToken = async () => {
      const token = await getCsrfToken();
      setCsrfToken(token);
    };
    fetchCsrfToken();
  }, [])
  

  useEffect(() => {
    const fetchCsrfToken = async () => {
      const token = await getCsrfToken();
      setCsrfToken(token);
    };
    fetchCsrfToken();
    const validate = async () => {
      try {
        const response = await axios.post(
          `http://localhost:8080/api/validate-token`,
          {
            userId: id,
            token: token,
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
        if (data.success) {
          setIsValid(true);
        } else {
          router.replace('/');
        }
      } catch (error) {
        if (error.response?.status === 400){
          router.replace('/');
        } else {
          setError(error.message);
        }
        
      }
    };

    if (token) validate();
  }, [token]);

  if (!isValid) {
    return <Spinner/>;
  }

  const deletePermission = async (permission) => {
    setSuccess("");
    setError("");
    try {
      const response = await axios.delete(
        `http://localhost:8080/api/delete-permission`,
        
        {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken,  // Jeśli korzystasz z JWT
          },
          params: {
            name: permission.dbName, 
            host: permission.dbHost,
            userId: id
          },
          withCredentials: true,  // Ustawienie to odpowiada za dołączanie ciasteczek
        }
      );

      const data = response.data;
      setSuccess(data.message);
      fetchPermissions();
    } catch (error) {
      if (error.response?.status === 400){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccess("");
    setError("");
    setPermissionsError("");
    try {
      const response = await axios.post(
        `http://localhost:8080/api/grant-permission`,
        {
          user: dbUser,
          host: host,
          database: database,
          password: password,
          port: port,
          userId: id,
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
      fetchPermissions();
      setDbUser("");
      setHost("");
      setDatabase("");
      setPassword("");
      setPort("");
    } catch (error) {
      if (error.response?.status === 400 || error.response?.status === 403 || error.response?.status === 404){
        setError(error.response?.data.message);
      } else {
        setError(error.message);
      }
      
    }finally {
      setLoading(false);
    }
    
  };

  const openModal = (permission) => {
    setPermissionToDelete(permission);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setPermissionToDelete(null);
  };

  const confirmDelete = async () => {
    closeModal();
    await deletePermission(permissionToDelete);
  };

  return (
    <StyledDiv style={{gap: '90px', marginTop: '50px'}}>
      <div style={{display: 'flex', gap: '80px'}}>
        <StyledDiv style={{border: '1px solid #ccc'}}>
          <h1>Username</h1>
          <p>{user.name}</p>
        </StyledDiv>
        <StyledDiv style={{border: '1px solid #ccc'}}>
          <h1>Email</h1>
          <p>{user.email}</p>
        </StyledDiv>
        
      </div>
      {/* <div style={{padding: '20px',borderBottom: '1px solid #eaeaea', width: '95%'}}></div> */}
      <StyledDiv style={{border: '1px solid #ccc'}}>
          <h1>Grant new permission of access to database</h1>
          <p>Enter data of database</p>
          <StyledForm style={{flexDirection: 'row'}}onSubmit={handleSubmit}>

            <div style={{flexDirection: 'column',display: 'flex'}}>
              <label>User</label>
              <input 
                type="text" 
                value={dbUser}
                onChange={(e) => setDbUser(e.target.value)}
                required 
              />
            </div>

            <div style={{flexDirection: 'column',display: 'flex'}}>
              <label>Host</label>
              <input 
                type="text" 
                value={host}
                onChange={(e) => setHost(e.target.value)}
                required 
              />
            </div>

            <div style={{flexDirection: 'column',display: 'flex'}}>
              <label>Database</label>
              <input 
                type="text" 
                value={database}
                onChange={(e) => setDatabase(e.target.value)}
                required 
              />
            </div>

            <div style={{flexDirection: 'column',display: 'flex'}}>
              <label>Password</label>
              <input 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required 
              />
            </div>

            <div style={{flexDirection: 'column',display: 'flex'}}>
              <label>Port</label>
              <input 
                type="text" 
                value={port}
                onChange={(e) => setPort(e.target.value)}
                required 
              />
            </div>

            <StyledButton type = "submit">Grant permission</StyledButton>
          </StyledForm>

        </StyledDiv>
      {/* <div style={{padding: '20px',borderTop: '1px solid #eaeaea', width: '95%'}}></div> */}
      <StyledDiv>
          <h1>Permitted Databases</h1>
          {permissionsError && <p style={{ color: "red" }}>{permissionsError}</p>}
          {permissions.length == 0 ? <p>Currently this user has no permission to access to any database.</p> :
          <ul>
            <StyledListItem>
              <div className="column">Database</div>
              <div className="column">Database User</div>
              <div className="column">Host</div>
              <div className="column">Port</div>
              <div className="actions"></div>
            </StyledListItem>
            {permissions.map(permission => (
              <StyledListItem key={permission.dbName}>
                <div className="column" style={{width: '90px'}}>{permission.dbName}</div>
                <div className="column" style={{width: '90px'}}>{permission.dbUser}</div>
                <div className="column" style={{width: '90px'}}>{permission.dbHost}</div>
                <div className="column" style={{width: '90px'}}>{permission.dbPort}</div>
                <div className="actions">
                  <StyledButton onClick={() => openModal(permission)}>
                    Delete permission
                  </StyledButton>
                </div>
              </StyledListItem>
            ))}
          </ul>}
          {isModalOpen && (
            <ConfirmModal
              message="Are you sure you want to remove this permission?"
              onConfirm={confirmDelete}
              onCancel={closeModal}
            />
          )}
          

          {loading && <Spinner/>}
          {error && <p style={{ color: "red" }}>{error}</p>}
          {success && <p style={{ color: "green" }}>{success}</p>}
        </StyledDiv>
      </StyledDiv>
      
  );
};

export default Permissions;