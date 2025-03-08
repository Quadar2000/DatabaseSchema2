"use client";

import React, { useEffect } from 'react';
import { useAuth } from '@/app/AuthProvider';
import { useRouter } from 'next/navigation';
import Spinner from '../Spinner/Spinner';

const WithRole = ({  children,role }) => {
  const { roles, loading, refreshUser } = useAuth();
  const router = useRouter();

  useEffect(() => {
    refreshUser();
  },[])
  

  if (loading) return <Spinner/>;
  if (roles !== role) {router.push('/');}

  return <>{children}</>;
};

export default WithRole