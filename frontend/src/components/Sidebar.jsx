import { useState, useEffect } from "react";
import { NavLink, Link, useLocation } from "react-router-dom";
import {
  Home,
  Users,
  Building,
  MessageSquare,
  CreditCard,
  Calendar,
  Megaphone,
  User,
  Heart,
  X,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import Logo from "./Logo";

const navigation = [
  {
    name: "Dashboard",
    href: "/dashboard",
    icon: Home,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
  {
    name: "Students",
    href: "/students",
    icon: Users,
    roles: ["ADMIN", "WARDEN"],
  },
  {
    name: "Rooms",
    href: "/rooms",
    icon: Building,
    roles: ["ADMIN", "WARDEN"],
  },
  {
    name: "Complaints",
    href: "/complaints",
    icon: MessageSquare,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
  {
    name: "Fees",
    href: "/fees",
    icon: CreditCard,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
  {
    name: "Leaves",
    href: "/leaves",
    icon: Calendar,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
  {
    name: "Announcements",
    href: "/announcements",
    icon: Megaphone,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
  {
    name: "Profile",
    href: "/profile",
    icon: User,
    roles: ["ADMIN", "WARDEN", "STUDENT"],
  },
];

const Sidebar = () => {
  const { user } = useAuth();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const filteredNavigation = navigation.filter((item) =>
    item.roles.includes(user?.role),
  );

  // The header's hamburger button sends a "toggle-sidebar" event
  useEffect(() => {
    const toggle = () => setMobileOpen((open) => !open);
    const onKey = (e) => {
      if (e.key === "Escape") setMobileOpen(false);
    };
    window.addEventListener("toggle-sidebar", toggle);
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("toggle-sidebar", toggle);
      window.removeEventListener("keydown", onKey);
    };
  }, []);

  // Close the drawer after moving to another page
  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  const panel = (isMobile) => (
    <div className="flex h-full w-72 flex-col overflow-y-auto bg-gradient-to-b from-[#0B1A45] to-[#0F2A63] pt-6 pb-5">
      <div className="flex items-center justify-between px-6">
        <Link
          to="/dashboard"
          className="flex items-center"
          aria-label="Go to dashboard"
        >
          <Logo className="h-14 w-14 text-white" />
          <div className="ml-3 leading-tight">
            <span className="block text-white text-2xl font-extrabold tracking-widest uppercase">
              HMS
            </span>
            <span className="block text-xs text-blue-200">
              Hostel Management
            </span>
          </div>
        </Link>
        {isMobile && (
          <button
            onClick={() => setMobileOpen(false)}
            aria-label="Close menu"
            className="rounded-md p-2 text-blue-100 hover:bg-white/10"
          >
            <X className="h-5 w-5" />
          </button>
        )}
      </div>

      <nav className="mt-10 flex-1 space-y-2 overflow-y-auto px-4">
        {filteredNavigation.map((item) => (
          <NavLink
            key={item.name}
            to={item.href}
            className={({ isActive }) =>
              `group flex items-center px-4 py-3 text-sm font-semibold rounded-xl transition-colors ${
                isActive
                  ? "bg-[#2D5FC4] text-white shadow-md"
                  : "text-blue-100 hover:bg-white/10 hover:text-white"
              }`
            }
          >
            <item.icon className="mr-4 h-5 w-5 flex-shrink-0" />
            {item.name}
          </NavLink>
        ))}
      </nav>

      <div className="flex items-center px-6 pt-4 text-sm text-blue-100">
        <Heart className="mr-2 h-4 w-4 fill-current" />
        Hostel Life, Better Together
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <div className="hidden lg:flex lg:flex-shrink-0">{panel(false)}</div>

      {/* Mobile drawer */}
      <div
        className={`lg:hidden fixed inset-0 z-40 ${
          mobileOpen ? "" : "pointer-events-none"
        }`}
        aria-hidden={!mobileOpen}
      >
        <div
          onClick={() => setMobileOpen(false)}
          className={`absolute inset-0 bg-black/50 transition-opacity duration-200 ${
            mobileOpen ? "opacity-100" : "opacity-0"
          }`}
        />
        <div
          className={`absolute inset-y-0 left-0 transform transition-transform duration-200 ${
            mobileOpen ? "translate-x-0" : "-translate-x-full"
          }`}
        >
          {panel(true)}
        </div>
      </div>
    </>
  );
};

export default Sidebar;
