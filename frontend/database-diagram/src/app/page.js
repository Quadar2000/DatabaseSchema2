"use client";
import { useAuth } from '@/app/AuthProvider';
import styles from "./page.module.css";
import SignIn from "./components/SignIn/SignIn";
import SchemaDiagram from "./components/SchemaDiagram/SchemaDiagram";
import Register from './register/page';
import Spinner from './components/Spinner/Spinner';

export default function Home() {

  const { user, loading } = useAuth();

  if(loading) {
    return <Spinner/>
  }

  if (user) {
    return (
      <main className={styles.main}>
        <SchemaDiagram/>
      </main>
      );
  }
    
  return (
    <main className={styles.main}>
      <SignIn/>
    </main>
    );

}
