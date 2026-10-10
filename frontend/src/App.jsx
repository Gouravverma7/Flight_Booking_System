import './App.css'
import { Route, Routes } from 'react-router-dom'
// import { Home } from 'lucide-react'
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import FlightResults from "./pages/FlightResults";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MyBookings from "./pages/MyBookings";
import Profile from "./pages/Profile";
import BookingDetails from "./pages/BookingDetails";
import Payment from "./pages/Payment";
import PaymentSuccess from "./pages/PaymentSuccess";
import Booking from "./pages/Booking";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";


function App() {

  return (
    <>
      <Navbar/>
      <Routes>
        <Route path='/' element={<Home/>}/>
        <Route path='/flights' element={<FlightResults/>}/>
        <Route path='/login' element={<Login/>}/>
        <Route path='/register' element={<Register/>}/>
        <Route path='/bookings' element={<MyBookings/>}/>
        <Route path='/profile' element={<Profile/>}/> 
        <Route path="/booking" element={<Booking />} />       
        <Route path='/bookings/:bookingReference' element={<BookingDetails/>}/>
        <Route path='/payment' element={<Payment/>}/>
        <Route path='/payment-success' element={<PaymentSuccess/>}/>
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/bookings" element={<MyBookings />} />
        <Route path="/bookings/:bookingReference" element={<BookingDetails />}
/>
      </Routes>
    </>
  )
}

export default App
