"use client";

import { useAuth } from '@/app/AuthProvider';
import React from 'react';
import { useRouter } from 'next/navigation';
import Spinner from '../Spinner/Spinner';

const IsLoggedIn = ({ children }) => {
  const { user, loading } = useAuth();
  const router = useRouter();

  if (loading) return <Spinner/>;
  if (!user) {
    router.push('/');
  }

  return <>{children}</>;
};

export default IsLoggedIn;