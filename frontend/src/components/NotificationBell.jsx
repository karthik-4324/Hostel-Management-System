import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bell,
  Megaphone,
  MessageSquare,
  Calendar,
  CreditCard,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import {
  announcementsAPI,
  complaintsAPI,
  leavesAPI,
  feesAPI,
} from "../services/api";

const POLL_MS = 60000;

const toMs = (value) => (value ? new Date(value).getTime() : 0);

const timeAgo = (ms) => {
  if (!ms) return "";
  const seconds = Math.floor((Date.now() - ms) / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
};

const kindStyle = {
  announcement: { Icon: Megaphone, box: "bg-blue-100 text-blue-700" },
  complaint: { Icon: MessageSquare, box: "bg-amber-100 text-amber-700" },
  leave: { Icon: Calendar, box: "bg-green-100 text-green-700" },
  fee: { Icon: CreditCard, box: "bg-purple-100 text-purple-700" },
  urgent: { Icon: CreditCard, box: "bg-red-100 text-red-700" },
};

const NotificationBell = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const containerRef = useRef(null);
  const uid = user?.id || user?.email || "user";
  const storageKey = `hms_notifications_seen_${uid}`;

  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const [lastSeen, setLastSeen] = useState(() => {
    try {
      return Number(localStorage.getItem(storageKey)) || 0;
    } catch (error) {
      return 0;
    }
  });

  const role = user?.role;

  const loadNotifications = useCallback(async () => {
    if (!role) return;

    const calls = {};
    if (role !== "ADMIN") {
      calls.announcements = announcementsAPI.getAll({ limit: 5 });
    }
    calls.complaints = complaintsAPI.getAll({ limit: 50 });
    calls.leaves = leavesAPI.getAll({ limit: 50 });
    if (role === "STUDENT") {
      calls.fees = feesAPI.getAll({ limit: 50 });
    }

    const keys = Object.keys(calls);
    const settled = await Promise.allSettled(keys.map((k) => calls[k]));
    const data = {};
    settled.forEach((result, i) => {
      if (result.status === "fulfilled" && result.value?.data?.success) {
        data[keys[i]] = result.value.data.data;
      }
    });

    const list = [];

    (data.announcements?.announcements || []).forEach((a) => {
      list.push({
        id: `a-${a.id}`,
        kind: "announcement",
        title: a.title,
        text: a.content,
        time: toMs(a.publishDate || a.createdAt),
        link: "/announcements",
      });
    });

    if (role === "STUDENT") {
      (data.complaints?.complaints || [])
        .filter((c) => c.status && c.status !== "PENDING")
        .forEach((c) => {
          list.push({
            id: `c-${c.id}-${c.status}`,
            kind: "complaint",
            title: "Complaint update",
            text: `"${c.title}" is now ${c.status.replace("_", " ").toLowerCase()}`,
            time: toMs(c.updatedAt || c.createdAt),
            link: "/complaints",
          });
        });

      (data.leaves?.leaves || [])
        .filter((l) => l.status === "APPROVED" || l.status === "REJECTED")
        .forEach((l) => {
          list.push({
            id: `l-${l.id}-${l.status}`,
            kind: "leave",
            title: `Leave ${l.status.toLowerCase()}`,
            text: `Your ${l.leaveType || ""} leave from ${new Date(l.startDate).toLocaleDateString()}`,
            time: toMs(l.updatedAt || l.createdAt),
            link: "/leaves",
          });
        });

      (data.fees?.fees || [])
        .filter((f) => f.balanceAmount > 0)
        .forEach((f) => {
          const overdue = f.status === "OVERDUE";
          list.push({
            id: `f-${f.id}-${f.status}`,
            kind: overdue ? "urgent" : "fee",
            title: overdue ? "Fee overdue" : "Fee pending",
            text: `₹${f.balanceAmount} due by ${new Date(f.dueDate).toLocaleDateString()}`,
            time: toMs(f.updatedAt || f.createdAt),
            link: "/fees",
          });
        });
    } else {
      (data.complaints?.complaints || [])
        .filter((c) => c.status === "PENDING")
        .forEach((c) => {
          list.push({
            id: `c-${c.id}`,
            kind: "complaint",
            title: "New complaint",
            text: `"${c.title}"`,
            time: toMs(c.createdAt),
            link: "/complaints",
          });
        });

      (data.leaves?.leaves || [])
        .filter((l) => l.status === "PENDING")
        .forEach((l) => {
          list.push({
            id: `l-${l.id}`,
            kind: "leave",
            title: "Leave request",
            text: `${l.leaveType || ""} leave from ${new Date(l.startDate).toLocaleDateString()}`,
            time: toMs(l.createdAt || l.appliedDate),
            link: "/leaves",
          });
        });
    }

    list.sort((a, b) => b.time - a.time);
    setItems(list.slice(0, 15));
  }, [role]);

  useEffect(() => {
    loadNotifications();
    const timer = setInterval(loadNotifications, POLL_MS);
    return () => clearInterval(timer);
  }, [loadNotifications]);

  useEffect(() => {
    if (!open) return;
    const onClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    const onEscape = (e) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("mousedown", onClickOutside);
    document.addEventListener("keydown", onEscape);
    return () => {
      document.removeEventListener("mousedown", onClickOutside);
      document.removeEventListener("keydown", onEscape);
    };
  }, [open]);

  const unreadCount = items.filter((i) => i.time > lastSeen).length;

  const handleToggle = () => {
    if (!open) loadNotifications();
    setOpen(!open);
  };

  const markAllRead = () => {
    const now = Date.now();
    try {
      localStorage.setItem(storageKey, String(now));
    } catch (error) {
      // storage not available, the dot just resets on refresh
    }
    setLastSeen(now);
  };

  const handleItemClick = (item) => {
    setOpen(false);
    navigate(item.link);
  };

  return (
    <div className="relative" ref={containerRef}>
      <button
        onClick={handleToggle}
        aria-label="Notifications"
        className="relative rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-[#0B1B45]"
      >
        <Bell className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute right-1.5 top-1.5 h-2.5 w-2.5 rounded-full bg-red-500 ring-2 ring-white" />
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-2 w-96 max-w-[90vw] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-xl">
          <div className="flex items-center justify-between border-b px-4 py-3">
            <h3 className="font-bold text-[#0B1B45]">
              Notifications
              {unreadCount > 0 && (
                <span className="ml-2 rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-600">
                  {unreadCount} new
                </span>
              )}
            </h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="text-xs font-semibold text-[#1D40B8] hover:underline"
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {items.length === 0 ? (
              <p className="px-4 py-8 text-center text-sm text-gray-500">
                You're all caught up.
              </p>
            ) : (
              items.map((item) => {
                const { Icon, box } = kindStyle[item.kind];
                const unread = item.time > lastSeen;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleItemClick(item)}
                    className={`flex w-full items-start px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
                      unread ? "bg-blue-50/60" : ""
                    }`}
                  >
                    <span
                      className={`mr-3 mt-0.5 flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full ${box}`}
                    >
                      <Icon className="h-4 w-4" />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span
                        className={`block truncate text-sm ${
                          unread
                            ? "font-bold text-[#0B1B45]"
                            : "font-medium text-gray-700"
                        }`}
                      >
                        {item.title}
                      </span>
                      <span className="block truncate text-sm text-gray-500">
                        {item.text}
                      </span>
                      <span className="mt-0.5 block text-xs text-gray-400">
                        {timeAgo(item.time)}
                      </span>
                    </span>
                    {unread && (
                      <span className="ml-2 mt-2 h-2 w-2 flex-shrink-0 rounded-full bg-[#1D40B8]" />
                    )}
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;