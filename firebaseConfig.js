// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyCPQTVSw9jmble2lzmQ-L9Xzag8NVi0Nf8",
  authDomain: "rasad-1fd8a.firebaseapp.com",
  projectId: "rasad-1fd8a",
  storageBucket: "rasad-1fd8a.firebasestorage.app",
  messagingSenderId: "1094448034582",
  appId: "1:1094448034582:web:a19e260a8834b812669637",
  measurementId: "G-845WS3R2LG"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);