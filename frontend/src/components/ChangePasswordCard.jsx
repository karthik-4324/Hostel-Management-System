import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { authAPI } from "../services/api";
import toast from "react-hot-toast";

const emptyForm = { currentPassword: "", newPassword: "", confirm: "" };

const ChangePasswordCard = () => {
  const [form, setForm] = useState(emptyForm);
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    if (form.newPassword.length < 8) {
      toast.error("New password must be at least 8 characters");
      return;
    }
    if (form.newPassword !== form.confirm) {
      toast.error("New passwords do not match");
      return;
    }
    setLoading(true);
    try {
      await authAPI.changePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      });
      toast.success("Password changed successfully");
      setForm(emptyForm);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to change password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="text-xl font-bold text-slate-800 tracking-tight">
          Change Password
        </h2>
      </div>
      <div className="card-content">
        <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Current password
            </label>
            <input
              type={show ? "text" : "password"}
              name="currentPassword"
              autoComplete="current-password"
              className="input mt-1"
              value={form.currentPassword}
              onChange={handleChange}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">
              New password (min 8 characters)
            </label>
            <div className="relative mt-1">
              <input
                type={show ? "text" : "password"}
                name="newPassword"
                autoComplete="new-password"
                className="input pr-10"
                value={form.newPassword}
                onChange={handleChange}
                minLength={8}
                required
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShow(!show)}
                aria-label={show ? "Hide passwords" : "Show passwords"}
              >
                {show ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Confirm new password
            </label>
            <input
              type={show ? "text" : "password"}
              name="confirm"
              autoComplete="new-password"
              className="input mt-1"
              value={form.confirm}
              onChange={handleChange}
              required
            />
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? "Saving..." : "Change Password"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordCard;
