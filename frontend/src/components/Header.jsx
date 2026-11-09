import { Menu, User, LogOut } from "lucide-react";
import { Link } from "react-router-dom";
import { Menu as HeadlessMenu } from "@headlessui/react";
import { useAuth } from "../contexts/AuthContext";
import NotificationBell from "./NotificationBell";
import Logo from "./Logo";

const Header = () => {
  const { user, logout } = useAuth();

  const openSidebar = () => window.dispatchEvent(new Event("toggle-sidebar"));

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="flex items-center justify-between px-6 py-4">
        <div className="flex items-center">
          <button
            onClick={openSidebar}
            aria-label="Open menu"
            className="mr-3 p-2 rounded-md text-gray-500 hover:bg-gray-100 lg:hidden"
          >
            <Menu className="h-6 w-6" />
          </button>
          <Link to="/dashboard" className="flex items-center">
            {/* <Logo className="mr-3 h-9 w-9 text-[#0B1B45]" /> */}
            <h1 className="text-lg sm:text-2xl font-extrabold text-slate-800 tracking-tight">
              Hostel Management System
            </h1>
          </Link>
        </div>

        <div className="flex items-center space-x-4">
          <NotificationBell />

          <HeadlessMenu as="div" className="relative">
            <HeadlessMenu.Button className="flex items-center space-x-3 p-2 rounded-md text-gray-700 hover:bg-gray-100">
              <div className="w-8 h-8 bg-[#1E2F94] rounded-full flex items-center justify-center">
                <User className="h-5 w-5 text-white" />
              </div>
              <div className="hidden md:block text-left">
                <p className="text-sm font-medium">
                  {user?.firstName} {user?.lastName}
                </p>
                <p className="text-xs text-gray-500">{user?.role}</p>
              </div>
            </HeadlessMenu.Button>

            <HeadlessMenu.Items className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none z-50">
              <div className="py-1">
                <HeadlessMenu.Item>
                  {({ active }) => (
                    <a
                      href="/profile"
                      className={`${
                        active ? "bg-gray-100" : ""
                      } flex items-center px-4 py-2 text-sm text-gray-700`}
                    >
                      <User className="mr-3 h-4 w-4" />
                      Profile
                    </a>
                  )}
                </HeadlessMenu.Item>
                <HeadlessMenu.Item>
                  {({ active }) => (
                    <button
                      onClick={logout}
                      className={`${
                        active ? "bg-gray-100" : ""
                      } flex items-center w-full px-4 py-2 text-sm text-gray-700`}
                    >
                      <LogOut className="mr-3 h-4 w-4" />
                      Sign out
                    </button>
                  )}
                </HeadlessMenu.Item>
              </div>
            </HeadlessMenu.Items>
          </HeadlessMenu>
        </div>
      </div>
    </header>
  );
};

export default Header;
