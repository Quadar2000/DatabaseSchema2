"use client";

import React from 'react';
import { useAuth } from '@/app/AuthProvider';
import { useRouter } from 'next/navigation';
import Spinner from '../Spinner/Spinner';

const WithRole = ({  children,role }) => {
  const { roles, loading, refreshUser } = useAuth();
  const router = useRouter();

  refreshUser();

  if (loading) return <Spinner/>;
  if (roles !== role) {router.push('/');}

  return <>{children}</>;
};

export default WithRole