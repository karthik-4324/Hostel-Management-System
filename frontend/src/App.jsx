import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./contexts/AuthContext";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Students from "./pages/Students";
import Rooms from "./pages/Rooms";
import Complaints from "./pages/Complaints";
import Fees from "./pages/Fees";
import Leaves from "./pages/Leaves";
import Announcements from "./pages/Announcements";
import Profile from "./pages/Profile";
import Staff from "./pages/Staff";
import LoadingSpinner from "./components/LoadingSpinner";

function App() {
  const { user, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={!user ? <Login /> : <Navigate to="/dashboard" />}
      />
      <Route
        path="/register"
        element={!user ? <Register /> : <Navigate to="/dashboard" />}
      />

      <Route path="/" element={user ? <Layout /> : <Navigate to="/login" />}>
        <Route index element={<Navigate to="/dashboard" />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="profile" element={<Profile />} />
        <Route path="staff" element={<Staff />} />

        {/* Admin/Warden Routes */}
        {(user?.role === "ADMIN" || user?.role === "WARDEN") && (
          <>
            <Route path="students" element={<Students />} />
            <Route path="rooms" element={<Rooms />} />
          </>
        )}

        {/* Common Routes */}
        <Route path="complaints" element={<Complaints />} />
        <Route path="fees" element={<Fees />} />
        <Route path="leaves" element={<Leaves />} />
        <Route path="announcements" element={<Announcements />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
}

export default App;
