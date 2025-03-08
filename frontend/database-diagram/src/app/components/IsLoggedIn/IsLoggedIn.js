"use client";

import { useAuth } from '@/app/AuthProvider';
import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Spinner from '../Spinner/Spinner';

const IsLoggedIn = ({ children }) => {
  const { user, loading,refreshUser } = useAuth();
  const router = useRouter();

  useEffect(() => {
      refreshUser();
    },[])

  if (loading) return <Spinner/>;
  if (!user) {
    router.push('/');
  }

  return <>{children}</>;
};

export default IsLoggedIn;