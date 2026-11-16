import { useState } from "react";
import { Link } from "react-router-dom";
import { authAPI } from "../services/api";
import Logo from "../components/Logo";
import toast from "react-hot-toast";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    try {
      await authAPI.forgotPassword(email.trim());
      setSent(true);
    } catch (error) {
      toast.error("Could not reach the server. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-[#0B1A45] to-[#0F2A63] py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="flex flex-col items-center">
          <Logo className="h-20 w-20 text-white" />
          <h1 className="mt-3 text-3xl font-extrabold tracking-widest uppercase text-white">
            HMS
          </h1>
          <p className="text-sm text-blue-200">Hostel Management System</p>
        </div>

        <div className="rounded-2xl bg-white p-8 shadow-xl">
          <h2 className="text-center text-2xl font-extrabold text-[#0B1B45]">
            Forgot your password?
          </h2>

          {sent ? (
            <div className="mt-6 space-y-4 text-center">
              <p className="text-gray-700">
                If an account exists for <strong>{email}</strong>, we have sent
                a link to reset the password. It works once and expires in 30
                minutes.
              </p>
              <p className="text-sm text-gray-500">
                Nothing arrived? Check your spam folder.
              </p>
              <Link
                to="/login"
                className="inline-block text-sm font-semibold text-[#1E2F94] hover:underline"
              >
                Back to sign in
              </Link>
            </div>
          ) : (
            <form className="mt-6 space-y-5" onSubmit={handleSubmit}>
              <p className="text-sm text-gray-600 text-center">
                Enter the email address you registered with and we will send
                you a reset link.
              </p>
              <div>
                <label
                  htmlFor="email"
                  className="block text-sm font-medium text-gray-700"
                >
                  Email address
                </label>
                <input
                  id="email"
                  type="email"
                  required
                  autoComplete="email"
                  className="input mt-1"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full justify-center py-3"
              >
                {loading ? "Sending..." : "Send reset link"}
              </button>

              <div className="text-center">
                <Link
                  to="/login"
                  className="text-sm font-semibold text-[#1E2F94] hover:underline"
                >
                  Back to sign in
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;