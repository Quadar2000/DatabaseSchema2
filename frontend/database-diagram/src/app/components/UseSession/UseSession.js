import { useEffect, useState } from 'react';

const UseSession = () => {
  const [session, setSession] = useState(null);

  useEffect(() => {
    const fetchSession = async () => {
      const response = await fetch('http://localhost:8080/api/auth/session-info', {
        method: 'GET',
        credentials: 'include',  // To ensures cookies (JSESSIONID) are sent
      });

      if (response.ok) {
        const data = await response.json();
        setSession(data);  // np. { sessionId: 'abc', roles: ['USER'] }
      } else {
        setSession(null);
      }
    };

    fetchSession();
  }, []);

  return session;
};

export default UseSession;