import axios from 'axios';

export const getCsrfToken = () => {
    const response = axios.get('http://localhost:8080/api/auth/csrf-token', { withCredentials: true });
    const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='));
    return csrfToken ? csrfToken.split('=')[1] : '';
  };

// import axios from 'axios';

// export async function getCsrfToken() {
//   const response = await axios.get('http://localhost:8080/api/auth/csrf-token', { withCredentials: true });
//   return response.data.token;
// }