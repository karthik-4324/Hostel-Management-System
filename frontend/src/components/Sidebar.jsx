import { NavLink } from "react-router-dom";
import {
  Home,
  Users,
  Building,
  MessageSquare,
  CreditCard,
  Calendar,
  Megaphone,
  User,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";

const Sidebar = () => {
  const { user } = useAuth();

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

  const filteredNavigation = navigation.filter((item) =>
    item.roles.includes(user?.role),
  );

  return (
    <div className="hidden lg:flex lg:flex-shrink-0">
      <div className="flex flex-col w-64">
        <div className="flex flex-col flex-grow bg-primary-700 pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 640 480"
              className="h-12 w-12 text-white"
              fill="currentColor"
              role="img"
              aria-label="Hospitality home logo"
            >
              <path d="M72 186 320 38l96 64V57h42v73l110 56L320 86 72 186Z" />
              <path d="M132 198h42v133h292v-95h42v178h-42v-41H174v41h-42V198Z" />
              <circle cx="221" cy="258" r="31" />
              <rect x="184" y="298" width="74" height="28" rx="4" />
              <path d="M270 257h120c35.35 0 64 28.65 64 64v5H270v-69Z" />
            </svg>
            <span className="ml-3 text-white text-2xl font-extrabold tracking-widest uppercase">
              HMS
            </span>
          </div>
          <nav className="mt-8 flex-1 flex flex-col divide-y divide-primary-800 overflow-y-auto">
            <div className="px-2 space-y-1">
              {filteredNavigation.map((item) => (
                <NavLink
                  key={item.name}
                  to={item.href}
                  className={({ isActive }) =>
                    `group flex items-center px-2 py-2 text-sm leading-6 font-medium rounded-md transition-colors ${
                      isActive
                        ? "bg-primary-800 text-white"
                        : "text-primary-200 hover:text-white hover:bg-primary-600"
                    }`
                  }
                >
                  <item.icon className="mr-4 h-6 w-6 flex-shrink-0" />
                  {item.name}
                </NavLink>
              ))}
            </div>
          </nav>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
