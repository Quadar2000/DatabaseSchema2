"use client";

import React from 'react';
import { useAuth } from '@/app/AuthProvider';
import { useRouter } from 'next/navigation';
import Spinner from '../Spinner/Spinner';

const WithRole = ({  children,role }) => {
  const { roles, loading } = useAuth();
  const router = useRouter();

  if (loading) return <Spinner/>;
  if (roles !== role) {router.push('/');}

  return <>{children}</>;
};

export default WithRole