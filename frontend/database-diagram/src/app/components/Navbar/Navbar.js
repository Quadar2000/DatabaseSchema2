"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from './Navbar.module.css';
import StyledButton from "../StyledButton/StyledButton";
import { useAuth } from "@/app/AuthProvider";

const Navbar = () => {
  const { user,roles, logOut } = useAuth();

  const handleLogout = async () => {
    await logOut();
  };

  if(!user){
    return <div></div>
  }
  return (
    <nav className={styles.navbar}>
      <ul className={styles.navLinks}>
        <li>
          <Link href="/">Home</Link>
        </li>
        <li>
          <Link href="/profile">Profile</Link>
        </li>
        {roles === 'ROLE_ADMIN' && (
          <>
            <li>
              <Link href="/register">Register</Link>
            </li>
            <li>
              <Link href="/users">Users List</Link>
            </li>
          </>
        )}
        <li>
          <StyledButton onClick={handleLogout}>Logout</StyledButton>
        </li>
      </ul>
    </nav>
  );
};

export default Navbar;