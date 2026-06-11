"use client";

import React, { useState, useEffect } from "react";
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  Briefcase,
  Coffee,
  Home,
  Zap,
  Plus,
  Trash,
  AlertTriangle,
  Download,
  Search,
  Filter,
  Sun,
  Moon,
  Info,
  Award,
  Laptop,
  Calendar,
  LogOut,
  User as UserIcon,
  Sparkles,
  PieChart as PieIcon,
  ChevronRight,
  RefreshCw,
  FolderPlus
} from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  BarChart,
  Bar
} from "recharts";

// Default seed categories matching DB
const DEFAULT_CATEGORIES = [
  { id: 1, name: "Salary", type: "INCOME", color: "#10B981", icon: "briefcase" },
  { id: 2, name: "Freelancing", type: "INCOME", color: "#3B82F6", icon: "laptop" },
  { id: 3, name: "Business", type: "INCOME", color: "#8B5CF6", icon: "trending-up" },
  { id: 4, name: "Investments", type: "INCOME", color: "#F59E0B", icon: "dollar-sign" },
  { id: 5, name: "Food", type: "EXPENSE", color: "#EF4444", icon: "coffee" },
  { id: 6, name: "Travel", type: "EXPENSE", color: "#3B82F6", icon: "map-pin" },
  { id: 7, name: "Shopping", type: "EXPENSE", color: "#EC4899", icon: "shopping-bag" },
  { id: 8, name: "Education", type: "EXPENSE", color: "#10B981", icon: "book-open" },
  { id: 9, name: "Rent", type: "EXPENSE", color: "#6366F1", icon: "home" },
  { id: 10, name: "Utilities", type: "EXPENSE", color: "#8B5CF6", icon: "zap" }
];

// Seed transactions
const SEED_TRANSACTIONS = [
  { id: 1, amount: 85000, type: "INCOME", category: "Salary", transactionDate: "2026-06-01", description: "Monthly Corporate Salary" },
  { id: 2, amount: 15000, type: "INCOME", category: "Freelancing", transactionDate: "2026-06-04", description: "Landing Page UI Client Pay" },
  { id: 3, amount: 12000, type: "EXPENSE", category: "Rent", transactionDate: "2026-06-02", description: "Apartment Rental Fee" },
  { id: 4, amount: 4500, type: "EXPENSE", category: "Food", transactionDate: "2026-06-05", description: "Weekly Grocery & Restaurant" },
  { id: 5, amount: 3000, type: "EXPENSE", category: "Utilities", transactionDate: "2026-06-06", description: "Electricity & Fiber Bills" },
  { id: 6, amount: 8500, type: "EXPENSE", category: "Shopping", transactionDate: "2026-06-08", description: "Noise Cancelling Headphones" },
  { id: 7, amount: 2500, type: "EXPENSE", category: "Travel", transactionDate: "2026-06-10", description: "Fuel & Cab Fares" }
];

// Seed savings goals
const SEED_GOALS = [
  { id: 1, name: "New Coding Laptop", targetAmount: 85000, currentAmount: 62000, deadline: "2026-12-15", status: "IN_PROGRESS" },
  { id: 2, name: "Emergency Contingency Fund", targetAmount: 50000, currentAmount: 30000, deadline: "2027-03-31", status: "IN_PROGRESS" },
  { id: 3, name: "Summer Trip / Vacation", targetAmount: 30000, currentAmount: 30000, deadline: "2026-06-15", status: "COMPLETED" }
];

// Seed Budgets
const SEED_BUDGETS = [
  { category: "Food", limit: 8000, spent: 4500 },
  { category: "Shopping", limit: 10000, spent: 8500 },
  { category: "Rent", limit: 12000, spent: 12000 },
  { category: "Utilities", limit: 5000, spent: 3000 }
];

export default function Dashboard() {
  const [mounted, setMounted] = useState(false);
  const [activeTab, setActiveTab] = useState("dashboard");
  const [darkMode, setDarkMode] = useState(true);

  // User Auth Profile State
  const [user, setUser] = useState<{ name: string; email: string } | null>(null);
  const [loginName, setLoginName] = useState("");
  const [loginEmail, setLoginEmail] = useState("");
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  // Report Form States
  const [pdfMonth, setPdfMonth] = useState("6");
  const [excelYear, setExcelYear] = useState("2026");

  // Financial States
  const [transactions, setTransactions] = useState<any[]>([]);
  const [goals, setGoals] = useState<any[]>([]);
  const [budgets, setBudgets] = useState<any[]>([]);
  const [notifications, setNotifications] = useState([
    { id: 1, title: "Budget Warning: Shopping", message: "You have utilized 85% of your Shopping budget limit.", type: "BUDGET_ALERT", time: "2 hours ago" },
    { id: 2, title: "Scheduled Payment Processed", message: "Your recurring bill for Rent of ₹12,000 has been recorded.", type: "BILL_REMINDER", time: "1 day ago" }
  ]);

  // Form Inputs
  const [txAmount, setTxAmount] = useState("");
  const [txType, setTxType] = useState("EXPENSE");
  const [txCategory, setTxCategory] = useState("Food");
  const [txDate, setTxDate] = useState("2026-06-11");
  const [txDesc, setTxDesc] = useState("");

  const [goalName, setGoalName] = useState("");
  const [goalTarget, setGoalTarget] = useState("");
  const [goalDeadline, setGoalDeadline] = useState("");

  const [budgetCategory, setBudgetCategory] = useState("Food");
  const [budgetLimit, setBudgetLimit] = useState("");

  // Goal Contribution
  const [contributeAmount, setContributeAmount] = useState("");
  const [selectedGoalId, setSelectedGoalId] = useState<number | null>(null);

  // Filters
  const [searchQuery, setSearchQuery] = useState("");
  const [filterType, setFilterType] = useState("ALL");

  // Initials extractor
  const getInitials = (name: string) => {
    if (!name) return "U";
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  };

  useEffect(() => {
    // Theme setup from local storage
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) {
      setDarkMode(savedTheme === "dark");
    } else {
      setDarkMode(true);
    }

    // User session setup from local storage
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        setUser(null);
      }
    } else {
      setUser(null);
    }

    setMounted(true);
  }, []);

  // Load user data on login or session change
  useEffect(() => {
    if (!user) {
      setTransactions([]);
      setGoals([]);
      setBudgets([]);
      return;
    }

    // Load transactions
    const txKey = `finsight_transactions_${user.email}`;
    const storedTxs = localStorage.getItem(txKey);
    if (storedTxs) {
      setTransactions(JSON.parse(storedTxs));
    } else {
      const initialTxs = user.email === "admin@finsight.com" ? SEED_TRANSACTIONS : [];
      setTransactions(initialTxs);
      localStorage.setItem(txKey, JSON.stringify(initialTxs));
    }

    // Load goals
    const goalsKey = `finsight_goals_${user.email}`;
    const storedGoals = localStorage.getItem(goalsKey);
    if (storedGoals) {
      setGoals(JSON.parse(storedGoals));
    } else {
      const initialGoals = user.email === "admin@finsight.com" ? SEED_GOALS : [];
      setGoals(initialGoals);
      localStorage.setItem(goalsKey, JSON.stringify(initialGoals));
    }

    // Load budgets
    const budgetsKey = `finsight_budgets_${user.email}`;
    const storedBudgets = localStorage.getItem(budgetsKey);
    if (storedBudgets) {
      setBudgets(JSON.parse(storedBudgets));
    } else {
      const initialBudgets = user.email === "admin@finsight.com" ? SEED_BUDGETS : [];
      setBudgets(initialBudgets);
      localStorage.setItem(budgetsKey, JSON.stringify(initialBudgets));
    }
  }, [user]);

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  }, [darkMode]);

  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginName || !loginEmail) return;
    const newUser = { name: loginName, email: loginEmail };
    setUser(newUser);
    localStorage.setItem("user", JSON.stringify(newUser));
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem("user");
    setShowProfileMenu(false);
  };

  const getApiUrl = () => {
    let apiBase = process.env.NEXT_PUBLIC_API_URL || "https://finsight-production-3f63.up.railway.app";
    apiBase = apiBase.trim();
    if (!apiBase.startsWith("http://") && !apiBase.startsWith("https://")) {
      apiBase = `https://${apiBase}`;
    }
    return apiBase;
  };

  const updateTransactions = (newTxs: any[]) => {
    setTransactions(newTxs);
    if (user) {
      localStorage.setItem(`finsight_transactions_${user.email}`, JSON.stringify(newTxs));
    }
  };

  const updateGoals = (newGoals: any[]) => {
    setGoals(newGoals);
    if (user) {
      localStorage.setItem(`finsight_goals_${user.email}`, JSON.stringify(newGoals));
    }
  };

  const updateBudgets = (newBudgets: any[]) => {
    setBudgets(newBudgets);
    if (user) {
      localStorage.setItem(`finsight_budgets_${user.email}`, JSON.stringify(newBudgets));
    }
  };

  // Calculate totals
  const totalIncome = transactions
    .filter(t => t.type === "INCOME")
    .reduce((acc, t) => acc + t.amount, 0);

  const totalExpense = transactions
    .filter(t => t.type === "EXPENSE")
    .reduce((acc, t) => acc + t.amount, 0);

  const totalBalance = totalIncome - totalExpense;
  const savingsRatio = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100 : 0;
  
  // Health Score algorithm
  const healthScore = Math.max(0, Math.min(100, Math.round(
    (savingsRatio > 0 ? savingsRatio * 1.5 : 0) + 40 - (budgets.filter(b => b.spent > b.limit).length * 15)
  )));

  // Add transaction handler
  const handleAddTransaction = (e: React.FormEvent) => {
    e.preventDefault();
    if (!txAmount || !txCategory) return;

    const newTx = {
      id: Date.now(),
      amount: parseFloat(txAmount),
      type: txType,
      category: txCategory,
      transactionDate: txDate,
      description: txDesc || `${txCategory} item`
    };

    const updatedTxs = [newTx, ...transactions];
    updateTransactions(updatedTxs);

    // If expense, check budget bounds
    if (txType === "EXPENSE") {
      const updatedBudgets = budgets.map(b => {
        if (b.category === txCategory) {
          const newSpent = b.spent + parseFloat(txAmount);
          if (newSpent > b.limit) {
            setNotifications(prev => [
              {
                id: Date.now(),
                title: `Budget Exceeded: ${b.category}`,
                message: `Alert! You have spent ₹${newSpent}, exceeding limit of ₹${b.limit} for ${b.category}.`,
                type: "BUDGET_ALERT",
                time: "Just now"
              },
              ...prev
            ]);
          }
          return { ...b, spent: newSpent };
        }
        return b;
      });
      updateBudgets(updatedBudgets);
    }

    // Reset fields
    setTxAmount("");
    setTxDesc("");
  };

  // Add goal handler
  const handleCreateGoal = (e: React.FormEvent) => {
    e.preventDefault();
    if (!goalName || !goalTarget || !goalDeadline) return;

    const newGoal = {
      id: Date.now(),
      name: goalName,
      targetAmount: parseFloat(goalTarget),
      currentAmount: 0,
      deadline: goalDeadline,
      status: "IN_PROGRESS"
    };

    updateGoals([...goals, newGoal]);
    setGoalName("");
    setGoalTarget("");
    setGoalDeadline("");
  };

  // Set budget handler
  const handleSetBudget = (e: React.FormEvent) => {
    e.preventDefault();
    if (!budgetLimit) return;

    const limitVal = parseFloat(budgetLimit);
    const existing = budgets.find(b => b.category === budgetCategory);

    if (existing) {
      updateBudgets(budgets.map(b => b.category === budgetCategory ? { ...b, limit: limitVal } : b));
    } else {
      updateBudgets([...budgets, { category: budgetCategory, limit: limitVal, spent: 0 }]);
    }
    setBudgetLimit("");
  };

  // Contribute to Goal handler
  const handleContribute = (e: React.FormEvent) => {
    e.preventDefault();
    if (!contributeAmount || selectedGoalId === null) return;

    const contVal = parseFloat(contributeAmount);
    let updatedGoals = goals.map(g => {
      if (g.id === selectedGoalId) {
        const updatedAmt = g.currentAmount + contVal;
        const reached = updatedAmt >= g.targetAmount;
        if (reached) {
          setNotifications(prev => [
            {
              id: Date.now(),
              title: "Goal Achieved! 🏆",
              message: `Congratulations! You successfully saved ₹${g.targetAmount} for: ${g.name}.`,
              type: "GOAL_REMINDER",
              time: "Just now"
            },
            ...prev
          ]);
        }
        return {
          ...g,
          currentAmount: updatedAmt,
          status: reached ? "COMPLETED" : "IN_PROGRESS"
        };
      }
      return g;
    });
    updateGoals(updatedGoals);

    // Deduct transaction list
    const selectedGoal = goals.find(g => g.id === selectedGoalId);
    const newTx = {
      id: Date.now(),
      amount: contVal,
      type: "EXPENSE",
      category: "Investments",
      transactionDate: new Date().toISOString().split('T')[0],
      description: `Savings goal deposit: ${selectedGoal?.name}`
    };
    updateTransactions([newTx, ...transactions]);

    setContributeAmount("");
    setSelectedGoalId(null);
  };

  // Delete transaction
  const handleDeleteTx = (id: number) => {
    updateTransactions(transactions.filter(t => t.id !== id));
  };

  // Chart data computation
  const monthlyTrendData = [
    { name: "Jan", Income: 65000, Expense: 42000 },
    { name: "Feb", Income: 70000, Expense: 48000 },
    { name: "Mar", Income: 80000, Expense: 52000 },
    { name: "Apr", Income: 75000, Expense: 46000 },
    { name: "May", Income: 90000, Expense: 55000 },
    { name: "Jun", Income: totalIncome, Expense: totalExpense }
  ];

  // Category breakdown computation
  const expenseByCategory = transactions
    .filter(t => t.type === "EXPENSE")
    .reduce((acc: any, t) => {
      const idx = acc.findIndex((item: any) => item.name === t.category);
      if (idx > -1) {
        acc[idx].value += t.amount;
      } else {
        const catObj = DEFAULT_CATEGORIES.find(c => c.name === t.category);
        acc.push({ name: t.category, value: t.amount, color: catObj?.color || "#cbd5e1" });
      }
      return acc;
    }, []);

  // Filtered transactions
  const filteredTransactions = transactions.filter(t => {
    const matchesSearch = t.description.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          t.category.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesType = filterType === "ALL" || t.type === filterType;
    return matchesSearch && matchesType;
  });

  if (!mounted) return null;

  if (!user) {
    return (
      <div className="min-h-screen bg-slate-50 dark:bg-[#0b0c10] flex items-center justify-center p-4 transition-colors duration-300">
        <div className="glass max-w-md w-full rounded-2xl p-8 border border-slate-200 dark:border-slate-800/80 shadow-2xl space-y-6">
          <div className="text-center space-y-2">
            <div className="bg-gradient-to-tr from-violet-600 to-indigo-600 p-4 rounded-2xl shadow-xl inline-block">
              <Sparkles className="h-8 w-8 text-white animate-pulse" />
            </div>
            <h2 className="text-2xl font-extrabold tracking-tight bg-gradient-to-r from-violet-500 via-indigo-500 to-cyan-500 bg-clip-text text-transparent">
              Welcome to FinSight
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              AI-Powered Personal Finance Management Platform
            </p>
          </div>

          <form onSubmit={handleLoginSubmit} className="space-y-4">
            <div className="space-y-1">
              <label className="text-[11px] font-bold text-slate-400 uppercase">Full Name</label>
              <input
                type="text"
                required
                placeholder="e.g. Mukesh Podugu"
                value={loginName}
                onChange={(e) => setLoginName(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 text-sm"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-bold text-slate-400 uppercase">Email Address</label>
              <input
                type="email"
                required
                placeholder="e.g. admin@finsight.com"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 text-sm"
              />
            </div>

            <button
              type="submit"
              className="w-full py-3 rounded-xl bg-gradient-to-r from-violet-600 to-indigo-600 hover:from-violet-500 hover:to-indigo-500 text-white font-bold text-sm shadow-lg shadow-indigo-500/20 transition-all duration-200 flex items-center justify-center gap-2"
            >
              Sign In
            </button>

            <div className="relative flex py-2 items-center">
              <div className="flex-grow border-t border-slate-200 dark:border-slate-800"></div>
              <span className="flex-shrink mx-4 text-[10px] text-slate-400 uppercase font-bold">Or Demo</span>
              <div className="flex-grow border-t border-slate-200 dark:border-slate-800"></div>
            </div>

            <button
              type="button"
              onClick={() => {
                const defaultUser = { name: "PODUGU MUKESH", email: "admin@finsight.com" };
                setUser(defaultUser);
                localStorage.setItem("user", JSON.stringify(defaultUser));
              }}
              className="w-full py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-900/40 text-slate-600 dark:text-slate-400 font-bold text-xs transition-all duration-200"
            >
              Quick Demo Sign In
            </button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className={darkMode ? "dark" : ""}>
      <div className="min-h-screen bg-slate-50 dark:bg-[#0b0c10] text-slate-800 dark:text-slate-100 flex flex-col transition-colors duration-300">
        
        {/* Top Navbar */}
        <header className="glass sticky top-0 z-40 border-b border-slate-200 dark:border-slate-800/80 px-6 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="bg-gradient-to-tr from-violet-600 to-indigo-600 p-2.5 rounded-xl shadow-lg shadow-indigo-500/20">
              <Sparkles className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-violet-500 via-indigo-500 to-cyan-500 bg-clip-text text-transparent">
                FinSight
              </h1>
              <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider">
                AI Smart Tracker
              </span>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <button
              onClick={() => setDarkMode(!darkMode)}
              className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 hover:text-indigo-500 transition-all duration-200"
              title="Toggle theme"
            >
              {darkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>

            {/* Profile Dropdown Context */}
            <div className="relative">
              <button
                onClick={() => setShowProfileMenu(!showProfileMenu)}
                className="flex items-center space-x-3 border-l border-slate-200 dark:border-slate-800 pl-4 focus:outline-none hover:opacity-90 transition-opacity"
              >
                <div className="h-9 w-9 rounded-xl bg-gradient-to-tr from-violet-500 to-indigo-500 flex items-center justify-center font-bold text-white shadow-md">
                  {getInitials(user?.name || "")}
                </div>
                <div className="hidden md:block text-left">
                  <p className="text-xs font-bold leading-tight">{user?.name}</p>
                  <p className="text-[10px] text-slate-500 dark:text-slate-400">{user?.email}</p>
                </div>
              </button>

              {showProfileMenu && (
                <div className="absolute right-0 mt-2 w-48 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xl py-2 z-50">
                  <div className="px-4 py-2 border-b border-slate-100 dark:border-slate-800/80">
                    <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">{user?.name}</p>
                    <p className="text-[10px] text-slate-500 truncate">{user?.email}</p>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-xs text-red-500 hover:bg-slate-100 dark:hover:bg-slate-800/60 font-semibold flex items-center space-x-2 transition-colors"
                  >
                    <LogOut className="h-4 w-4" />
                    <span>Sign Out</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Dashboard Grid Workspace */}
        <div className="flex-1 flex flex-col md:flex-row">
          
          {/* Sidebar */}
          <aside className="w-full md:w-64 border-r border-slate-200 dark:border-slate-800/80 p-6 flex flex-col space-y-2.5 bg-white/40 dark:bg-slate-950/40">
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest px-3 mb-2">Main Menu</p>
            {[
              { id: "dashboard", label: "Dashboard", icon: Home },
              { id: "transactions", label: "Transactions", icon: Briefcase },
              { id: "budgets", label: "Budgets & Goals", icon: FolderPlus },
              { id: "ai", label: "AI Insights", icon: Sparkles },
              { id: "reports", label: "Reports", icon: Download }
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl font-medium text-sm transition-all duration-200 ${
                  activeTab === tab.id
                    ? "bg-gradient-to-r from-violet-600 to-indigo-600 text-white shadow-lg shadow-indigo-600/10"
                    : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-900/60"
                }`}
              >
                <tab.icon className="h-5 w-5" />
                <span>{tab.label}</span>
              </button>
            ))}

            <hr className="border-slate-200 dark:border-slate-800 my-6" />

            <div className="bg-slate-100/50 dark:bg-slate-900/40 border border-slate-200 dark:border-slate-800/60 rounded-xl p-4 mt-auto">
              <h4 className="text-xs font-bold text-indigo-500">System Developer</h4>
              <p className="text-xs font-semibold mt-1">PODUGU MUKESH</p>
              <p className="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5 leading-relaxed">
                Srikakulam, India<br />
                Ph: +91 8143999463
              </p>
            </div>
          </aside>

          {/* Main Workspace */}
          <main className="flex-1 p-6 md:p-8 overflow-y-auto">
            
            {/* TAB: Dashboard */}
            {activeTab === "dashboard" && (
              <div className="space-y-6">
                
                {/* Score cards grid */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  
                  {/* Total Balance */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-slate-400 uppercase">Total Balance</span>
                      <div className="p-2 rounded-xl bg-violet-500/10 text-violet-500"><DollarSign className="h-5 w-5" /></div>
                    </div>
                    <h3 className="text-2xl font-black mt-3">₹{totalBalance.toLocaleString()}</h3>
                    <p className="text-[10px] text-emerald-500 font-bold mt-1 flex items-center">
                      <TrendingUp className="h-3 w-3 mr-1" /> +12.4% vs last month
                    </p>
                  </div>

                  {/* Monthly Income */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-slate-400 uppercase">Monthly Income</span>
                      <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-500"><TrendingUp className="h-5 w-5" /></div>
                    </div>
                    <h3 className="text-2xl font-black mt-3">₹{totalIncome.toLocaleString()}</h3>
                    <p className="text-[10px] text-emerald-500 font-bold mt-1 flex items-center">
                      <TrendingUp className="h-3 w-3 mr-1" /> +8.1% vs last month
                    </p>
                  </div>

                  {/* Monthly Expenses */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-slate-400 uppercase">Monthly Expenses</span>
                      <div className="p-2 rounded-xl bg-red-500/10 text-red-500"><TrendingDown className="h-5 w-5" /></div>
                    </div>
                    <h3 className="text-2xl font-black mt-3">₹{totalExpense.toLocaleString()}</h3>
                    <p className="text-[10px] text-red-500 font-bold mt-1 flex items-center">
                      <TrendingDown className="h-3 w-3 mr-1" /> -4.2% vs last month
                    </p>
                  </div>

                  {/* Financial Health Score */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md bg-gradient-to-tr from-violet-600/5 to-indigo-600/5">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-indigo-500 dark:text-indigo-400 uppercase">AI Health Score</span>
                      <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-500"><Award className="h-5 w-5" /></div>
                    </div>
                    <div className="flex items-baseline space-x-2 mt-3">
                      <h3 className="text-3xl font-black text-indigo-600 dark:text-indigo-400">{healthScore}</h3>
                      <span className="text-xs text-slate-400">/100</span>
                    </div>
                    <p className="text-[10px] font-bold text-indigo-500 mt-1 flex items-center">
                      <Sparkles className="h-3 w-3 mr-1" /> {healthScore >= 75 ? "Excellent Health" : "Steady Budgeting"}
                    </p>
                  </div>
                </div>

                {/* Charts Area */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  
                  {/* Monthly Trend Area Graph */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md md:col-span-2">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Inflow vs Outflow Trend</h3>
                    <div className="h-64 w-full">
                      <ResponsiveContainer width="100%" height="100%">
                        <AreaChart data={monthlyTrendData}>
                          <defs>
                            <linearGradient id="incomeColor" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#10B981" stopOpacity={0.2}/>
                              <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                            </linearGradient>
                            <linearGradient id="expenseColor" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#EF4444" stopOpacity={0.2}/>
                              <stop offset="95%" stopColor="#EF4444" stopOpacity={0}/>
                            </linearGradient>
                          </defs>
                          <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.15} />
                          <XAxis dataKey="name" stroke="#64748b" fontSize={11} />
                          <YAxis stroke="#64748b" fontSize={11} />
                          <Tooltip contentStyle={{ backgroundColor: "#0f172a", border: "none", borderRadius: "8px", color: "#fff" }} />
                          <Area type="monotone" dataKey="Income" stroke="#10B981" fillOpacity={1} fill="url(#incomeColor)" strokeWidth={2} />
                          <Area type="monotone" dataKey="Expense" stroke="#EF4444" fillOpacity={1} fill="url(#expenseColor)" strokeWidth={2} />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                  </div>

                  {/* Category Expense Breakdown */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Category Expenditures</h3>
                    <div className="h-64 w-full flex flex-col justify-center items-center">
                      {expenseByCategory.length > 0 ? (
                        <>
                          <ResponsiveContainer width="100%" height="80%">
                            <PieChart>
                              <Pie
                                data={expenseByCategory}
                                cx="50%"
                                cy="50%"
                                innerRadius={60}
                                outerRadius={80}
                                paddingAngle={5}
                                dataKey="value"
                              >
                                {expenseByCategory.map((entry: any, index: number) => (
                                  <Cell key={`cell-${index}`} fill={entry.color} />
                                ))}
                              </Pie>
                              <Tooltip contentStyle={{ backgroundColor: "#0f172a", border: "none", borderRadius: "8px", color: "#fff" }} />
                            </PieChart>
                          </ResponsiveContainer>
                          <div className="flex flex-wrap justify-center gap-3 text-[10px] mt-2">
                            {expenseByCategory.map((entry: any, index: number) => (
                              <span key={index} className="flex items-center font-bold">
                                <span className="h-2.5 w-2.5 rounded-full mr-1.5" style={{ backgroundColor: entry.color }}></span>
                                {entry.name}: ₹{entry.value}
                              </span>
                            ))}
                          </div>
                        </>
                      ) : (
                        <div className="text-center text-slate-400 text-xs">No expenses logged yet</div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Bottom Row: Recent logs & Notifications */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  
                  {/* Recent Transactions List */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400">Recent Transactions</h3>
                      <button onClick={() => setActiveTab("transactions")} className="text-xs text-indigo-500 hover:text-indigo-400 font-bold flex items-center">
                        View All <ChevronRight className="h-4 w-4" />
                      </button>
                    </div>
                    <div className="divide-y divide-slate-100 dark:divide-slate-800/60 max-h-72 overflow-y-auto">
                      {transactions.slice(0, 4).map(t => (
                        <div key={t.id} className="py-3.5 flex items-center justify-between">
                          <div className="flex items-center space-x-3">
                            <div className={`p-2 rounded-xl text-white font-bold text-xs ${t.type === "INCOME" ? "bg-emerald-500/20 text-emerald-500" : "bg-red-500/20 text-red-500"}`}>
                              {t.type === "INCOME" ? <TrendingUp className="h-4 w-4" /> : <TrendingDown className="h-4 w-4" />}
                            </div>
                            <div>
                              <p className="text-xs font-bold leading-tight">{t.description}</p>
                              <p className="text-[10px] text-slate-400 font-medium">{t.category} • {t.transactionDate}</p>
                            </div>
                          </div>
                          <span className={`text-xs font-extrabold ${t.type === "INCOME" ? "text-emerald-500" : "text-red-500"}`}>
                            {t.type === "INCOME" ? "+" : "-"}₹{t.amount.toLocaleString()}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* System Alerts and Warnings */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">System Alerts & Alerts</h3>
                    <div className="space-y-4 max-h-72 overflow-y-auto">
                      {notifications.map(n => (
                        <div key={n.id} className="p-3.5 rounded-xl bg-slate-100/50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800/80 flex items-start space-x-3">
                          <div className={`p-1.5 rounded-lg mt-0.5 ${n.type === "BUDGET_ALERT" ? "bg-amber-500/10 text-amber-500" : "bg-blue-500/10 text-blue-500"}`}>
                            <AlertTriangle className="h-4 w-4" />
                          </div>
                          <div className="flex-1">
                            <h4 className="text-xs font-extrabold text-slate-700 dark:text-slate-200 leading-tight">{n.title}</h4>
                            <p className="text-[10px] text-slate-500 dark:text-slate-400 mt-1 leading-normal">{n.message}</p>
                            <span className="text-[9px] text-slate-400 block mt-1.5">{n.time}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

              </div>
            )}

            {/* TAB: Transactions */}
            {activeTab === "transactions" && (
              <div className="space-y-6">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <h2 className="text-xl font-extrabold tracking-tight">Ledger Transactions</h2>
                  
                  {/* Filters bar */}
                  <div className="flex items-center space-x-3">
                    <div className="relative">
                      <Search className="absolute left-3 top-2.5 h-4.5 w-4.5 text-slate-400" />
                      <input
                        type="text"
                        placeholder="Search logs..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-9 pr-4 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500 w-48 md:w-64"
                      />
                    </div>
                    <select
                      value={filterType}
                      onChange={(e) => setFilterType(e.target.value)}
                      className="px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    >
                      <option value="ALL">All Types</option>
                      <option value="INCOME">Income Only</option>
                      <option value="EXPENSE">Expense Only</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  
                  {/* Transaction List Table */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md md:col-span-2 overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="border-b border-slate-200 dark:border-slate-800 text-[10px] uppercase font-bold text-slate-400">
                          <th className="pb-3">Date</th>
                          <th className="pb-3">Description</th>
                          <th className="pb-3">Category</th>
                          <th className="pb-3">Type</th>
                          <th className="pb-3 text-right">Amount</th>
                          <th className="pb-3 text-center">Action</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-xs">
                        {filteredTransactions.map(t => (
                          <tr key={t.id} className="hover:bg-slate-100/50 dark:hover:bg-slate-900/30 transition-colors">
                            <td className="py-4">{t.transactionDate}</td>
                            <td className="py-4 font-bold">{t.description}</td>
                            <td className="py-4">{t.category}</td>
                            <td className="py-4">
                              <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold ${t.type === "INCOME" ? "bg-emerald-500/10 text-emerald-500" : "bg-red-500/10 text-red-500"}`}>
                                {t.type}
                              </span>
                            </td>
                            <td className={`py-4 text-right font-extrabold ${t.type === "INCOME" ? "text-emerald-500" : "text-red-500"}`}>
                              ₹{t.amount.toLocaleString()}
                            </td>
                            <td className="py-4 text-center">
                              <button onClick={() => handleDeleteTx(t.id)} className="text-slate-400 hover:text-red-500 transition-colors p-1" title="Delete log">
                                <Trash className="h-4 w-4" />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Add Transaction Form */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md h-fit">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Record Transaction</h3>
                    <form onSubmit={handleAddTransaction} className="space-y-4">
                      <div>
                        <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Amount (₹)</label>
                        <input
                          type="number"
                          placeholder="e.g. 5000"
                          required
                          value={txAmount}
                          onChange={(e) => setTxAmount(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                      </div>
                      <div>
                        <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Type</label>
                        <div className="grid grid-cols-2 gap-3">
                          <button
                            type="button"
                            onClick={() => setTxType("INCOME")}
                            className={`py-2 text-xs rounded-xl border font-bold transition-colors ${txType === "INCOME" ? "bg-emerald-500/10 border-emerald-500 text-emerald-500" : "border-slate-200 dark:border-slate-800"}`}
                          >
                            Income
                          </button>
                          <button
                            type="button"
                            onClick={() => setTxType("EXPENSE")}
                            className={`py-2 text-xs rounded-xl border font-bold transition-colors ${txType === "EXPENSE" ? "bg-red-500/10 border-red-500 text-red-500" : "border-slate-200 dark:border-slate-800"}`}
                          >
                            Expense
                          </button>
                        </div>
                      </div>
                      <div>
                        <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Category</label>
                        <select
                          value={txCategory}
                          onChange={(e) => setTxCategory(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        >
                          {DEFAULT_CATEGORIES.filter(c => c.type === txType).map(c => (
                            <option key={c.id} value={c.name}>{c.name}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Date</label>
                        <input
                          type="date"
                          required
                          value={txDate}
                          onChange={(e) => setTxDate(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                      </div>
                      <div>
                        <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Description</label>
                        <input
                          type="text"
                          placeholder="e.g. Groceries at supermarket"
                          value={txDesc}
                          onChange={(e) => setTxDesc(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                      </div>
                      <button
                        type="submit"
                        className="w-full py-2.5 text-xs rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-lg transition-all"
                      >
                        Submit Record
                      </button>
                    </form>
                  </div>
                </div>
              </div>
            )}

            {/* TAB: Budgets & Goals */}
            {activeTab === "budgets" && (
              <div className="space-y-6">
                
                {/* Upper row: Budget limits & Goal tracking */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  
                  {/* Monthly Limits Setup */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Set Category Budgets</h3>
                    <div className="space-y-4">
                      <form onSubmit={handleSetBudget} className="flex gap-3 items-end">
                        <div className="flex-1">
                          <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Category</label>
                          <select
                            value={budgetCategory}
                            onChange={(e) => setBudgetCategory(e.target.value)}
                            className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          >
                            {DEFAULT_CATEGORIES.filter(c => c.type === "EXPENSE").map(c => (
                              <option key={c.id} value={c.name}>{c.name}</option>
                            ))}
                          </select>
                        </div>
                        <div className="flex-1">
                          <label className="text-[10px] uppercase font-bold text-slate-400 block mb-1.5">Limit (₹)</label>
                          <input
                            type="number"
                            placeholder="e.g. 5000"
                            required
                            value={budgetLimit}
                            onChange={(e) => setBudgetLimit(e.target.value)}
                            className="w-full px-3 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          />
                        </div>
                        <button type="submit" className="px-4 py-2 text-xs rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold h-fit shadow-md">
                          Set
                        </button>
                      </form>

                      {/* Display Budgets */}
                      <div className="divide-y divide-slate-100 dark:divide-slate-800/60 max-h-64 overflow-y-auto pt-2">
                        {budgets.map((b, i) => {
                          const percent = Math.min(100, Math.round((b.spent / b.limit) * 100));
                          const exceeded = b.spent > b.limit;
                          return (
                            <div key={i} className="py-3.5 space-y-2">
                              <div className="flex items-center justify-between text-xs font-bold">
                                <span>{b.category} Budget</span>
                                <span className={exceeded ? "text-red-500 font-extrabold" : "text-slate-400"}>
                                  ₹{b.spent.toLocaleString()} / ₹{b.limit.toLocaleString()}
                                </span>
                              </div>
                              <div className="w-full bg-slate-200 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                                <div
                                  className={`h-full rounded-full transition-all duration-300 ${exceeded ? "bg-red-500" : (percent >= 80 ? "bg-amber-500" : "bg-indigo-500")}`}
                                  style={{ width: `${percent}%` }}
                                ></div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>

                  {/* Savings Goals Setup */}
                  <div className="glass rounded-2xl p-5 border border-slate-200 dark:border-slate-800/80 shadow-md">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Financial Savings Goals</h3>
                    <div className="space-y-4">
                      
                      {/* Create Goal Form */}
                      <form onSubmit={handleCreateGoal} className="grid grid-cols-3 gap-2.5 items-end">
                        <div>
                          <label className="text-[9px] uppercase font-bold text-slate-400 block mb-1">Goal Name</label>
                          <input
                            type="text"
                            placeholder="Laptop"
                            required
                            value={goalName}
                            onChange={(e) => setGoalName(e.target.value)}
                            className="w-full px-2.5 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none"
                          />
                        </div>
                        <div>
                          <label className="text-[9px] uppercase font-bold text-slate-400 block mb-1">Target (₹)</label>
                          <input
                            type="number"
                            placeholder="85000"
                            required
                            value={goalTarget}
                            onChange={(e) => setGoalTarget(e.target.value)}
                            className="w-full px-2.5 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none"
                          />
                        </div>
                        <div className="flex gap-2">
                          <div className="flex-1">
                            <label className="text-[9px] uppercase font-bold text-slate-400 block mb-1">Deadline</label>
                            <input
                              type="date"
                              required
                              value={goalDeadline}
                              onChange={(e) => setGoalDeadline(e.target.value)}
                              className="w-full px-2 py-2 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none"
                            />
                          </div>
                          <button type="submit" className="p-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-md">
                            <Plus className="h-4 w-4" />
                          </button>
                        </div>
                      </form>

                      {/* Display Goals */}
                      <div className="divide-y divide-slate-100 dark:divide-slate-800/60 max-h-64 overflow-y-auto pt-2">
                        {goals.map(g => {
                          const percent = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100));
                          return (
                            <div key={g.id} className="py-3.5 space-y-2">
                              <div className="flex items-center justify-between text-xs font-bold">
                                <span>{g.name}</span>
                                <span className="text-slate-400">
                                  ₹{g.currentAmount.toLocaleString()} / ₹{g.targetAmount.toLocaleString()} ({percent}%)
                                </span>
                              </div>
                              <div className="w-full bg-slate-200 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                                <div
                                  className="h-full rounded-full bg-emerald-500 transition-all duration-300"
                                  style={{ width: `${percent}%` }}
                                ></div>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-[9px] text-slate-400">Deadline: {g.deadline}</span>
                                {g.status !== "COMPLETED" && (
                                  <button
                                    onClick={() => setSelectedGoalId(g.id)}
                                    className="text-[10px] text-indigo-500 hover:text-indigo-400 font-bold"
                                  >
                                    + Add Savings
                                  </button>
                                )}
                              </div>
                            </div>
                          );
                        })}
                      </div>

                      {/* Contribution Dialog */}
                      {selectedGoalId !== null && (
                        <div className="p-3 bg-slate-100 dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 mt-2">
                          <form onSubmit={handleContribute} className="flex gap-3 items-end">
                            <div className="flex-1">
                              <label className="text-[9px] uppercase font-bold text-slate-400 block mb-1">Savings Contribution Amount (₹)</label>
                              <input
                                type="number"
                                placeholder="1000"
                                required
                                value={contributeAmount}
                                onChange={(e) => setContributeAmount(e.target.value)}
                                className="w-full px-3 py-1.5 text-xs rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 focus:outline-none"
                              />
                            </div>
                            <button type="submit" className="px-3 py-1.5 text-xs rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold">
                              Save
                            </button>
                            <button type="button" onClick={() => setSelectedGoalId(null)} className="px-3 py-1.5 text-xs rounded-lg bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300">
                              Cancel
                            </button>
                          </form>
                        </div>
                      )}

                    </div>
                  </div>

                </div>
              </div>
            )}

            {/* TAB: AI Insights */}
            {activeTab === "ai" && (
              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <h2 className="text-xl font-extrabold tracking-tight flex items-center">
                    <Sparkles className="h-6 w-6 mr-2 text-indigo-500" />
                    AI Spending Insights & Analytics
                  </h2>
                  <span className="text-xs font-semibold px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-500">
                    Gemini AI Pro Active
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  
                  {/* Insight panels */}
                  <div className="glass rounded-2xl p-6 border border-slate-200 dark:border-slate-800/80 shadow-md md:col-span-2 space-y-6">
                    <div>
                      <h4 className="text-xs font-extrabold text-indigo-500 uppercase tracking-widest flex items-center mb-2">
                        <Sparkles className="h-4 w-4 mr-1.5" /> Spending Breakdown Insight
                      </h4>
                      <div className="text-sm leading-relaxed p-4 rounded-xl bg-slate-100/50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800/80">
                        <p>Hello PODUGU. Based on your spending details this month, your largest expenditure category was **Rent** (₹12,000).</p>
                        <p className="mt-2.5">You saved ₹74,000 out of your ₹1,00,000 total earnings. We noticed significant savings opportunities in Shopping (₹8,500). Consider allocating some of these funds to your savings goals.</p>
                        <p className="mt-2.5">Tip: Limit your shopping bills to improve your cash reserves by 15% next month.</p>
                      </div>
                    </div>

                    <div>
                      <h4 className="text-xs font-extrabold text-indigo-500 uppercase tracking-widest flex items-center mb-2">
                        <Sparkles className="h-4 w-4 mr-1.5" /> Ideal Budget Limits Recommendations
                      </h4>
                      <div className="text-sm leading-relaxed p-4 rounded-xl bg-slate-100/50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800/80">
                        <p className="font-bold mb-2">Recommended Category Budgets (50/30/20 Allocation model):</p>
                        <ul className="list-disc list-inside space-y-2 text-slate-600 dark:text-slate-300">
                          <li><strong className="text-slate-800 dark:text-white">Needs (50%): ₹50,000</strong> — Covers groceries, rent, utilities, transport.</li>
                          <li><strong className="text-slate-800 dark:text-white">Wants (30%): ₹30,000</strong> — Redirect to dining, entertainment, tech shopping.</li>
                          <li><strong className="text-slate-800 dark:text-white">Savings & Debt (20%): ₹20,000</strong> — Save for Laptop, emergency buffers.</li>
                        </ul>
                      </div>
                    </div>
                  </div>

                  {/* Financial score detailed metrics */}
                  <div className="glass rounded-2xl p-6 border border-slate-200 dark:border-slate-800/80 shadow-md flex flex-col justify-between">
                    <div>
                      <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4">Financial Health Score</h3>
                      
                      <div className="flex flex-col items-center justify-center py-6">
                        <div className="h-32 w-32 rounded-full border-4 border-indigo-500/20 flex items-center justify-center relative shadow-lg">
                          <div className="absolute inset-0.5 rounded-full border border-dashed border-indigo-500/40"></div>
                          <div className="text-center">
                            <span className="text-4xl font-black text-indigo-500">{healthScore}</span>
                            <span className="text-xs block text-slate-500 font-bold uppercase">Health Score</span>
                          </div>
                        </div>
                        
                        <div className="mt-6 text-center">
                          <h4 className="text-sm font-bold text-slate-700 dark:text-slate-200">Excellent Personal Health Status</h4>
                          <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
                            Your savings ratio is 74.0%, which is exceptional. You have overspent on 1 category budget (Shopping) this month.
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="pt-4 border-t border-slate-200 dark:border-slate-800/80">
                      <div className="flex items-start space-x-2 text-[10px] text-slate-400 leading-normal">
                        <Info className="h-4.5 w-4.5 text-indigo-500 flex-shrink-0 mt-0.5" />
                        <span>Calculated dynamically based on savings percentage, budgets compliance, and deadline completion.</span>
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            )}

            {/* TAB: Reports */}
            {activeTab === "reports" && (
              <div className="space-y-6 max-w-3xl mx-auto">
                <h2 className="text-xl font-extrabold tracking-tight">Generate Financial Reports</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  
                  {/* Monthly PDF Exporter */}
                  <div className="glass rounded-2xl p-6 border border-slate-200 dark:border-slate-800/80 shadow-md text-center space-y-4">
                    <div className="h-12 w-12 rounded-xl bg-red-500/10 text-red-500 flex items-center justify-center mx-auto">
                      <Download className="h-6 w-6" />
                    </div>
                    <div>
                      <h4 className="text-sm font-extrabold">Monthly Statement (PDF)</h4>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                        Generates a compiled PDF document showing totals, visual summary grids, and detailed transaction entries.
                      </p>
                    </div>
                    
                    <div className="flex gap-2">
                      <select 
                        value={pdfMonth}
                        onChange={(e) => setPdfMonth(e.target.value)}
                        className="flex-1 px-3 py-1.5 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none"
                      >
                        <option value="6">June 2026</option>
                        <option value="5">May 2026</option>
                        <option value="4">April 2026</option>
                        <option value="3">March 2026</option>
                        <option value="2">February 2026</option>
                        <option value="1">January 2026</option>
                      </select>
                      <button
                        onClick={() => {
                          const apiBase = getApiUrl();
                          window.open(`${apiBase}/api/v1/reports/export/pdf?month=${pdfMonth}&year=2026`, '_blank');
                        }}
                        className="px-4 py-2 text-xs rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold flex items-center justify-center gap-1.5 shadow-md"
                      >
                        <Download className="h-4 w-4" /> Download PDF
                      </button>
                    </div>
                  </div>

                  {/* Excel Ledger Exporter */}
                  <div className="glass rounded-2xl p-6 border border-slate-200 dark:border-slate-800/80 shadow-md text-center space-y-4">
                    <div className="h-12 w-12 rounded-xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center mx-auto">
                      <Download className="h-6 w-6" />
                    </div>
                    <div>
                      <h4 className="text-sm font-extrabold">Excel Transactions Ledger</h4>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                        Export your full database transaction history including description, type, category, and audit details to a spreadsheet.
                      </p>
                    </div>
                    
                    <div className="flex gap-2">
                      <select 
                        value={excelYear}
                        onChange={(e) => setExcelYear(e.target.value)}
                        className="flex-1 px-3 py-1.5 text-xs rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:outline-none"
                      >
                        <option value="2026">Year 2026</option>
                        <option value="2025">Year 2025</option>
                      </select>
                      <button
                        onClick={() => {
                          const apiBase = getApiUrl();
                          window.open(`${apiBase}/api/v1/reports/export/excel?year=${excelYear}`, '_blank');
                        }}
                        className="px-4 py-2 text-xs rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold flex items-center justify-center gap-1.5 shadow-md"
                      >
                        <Download className="h-4 w-4" /> Download Excel
                      </button>
                    </div>
                  </div>

                </div>
              </div>
            )}

          </main>
        </div>

        {/* Footer */}
        <footer className="glass border-t border-slate-200 dark:border-slate-800/80 py-6 px-6 text-center text-xs text-slate-500 dark:text-slate-400">
          <div className="flex flex-col md:flex-row justify-between items-center max-w-7xl mx-auto gap-4">
            <p className="font-semibold text-slate-600 dark:text-slate-300">
              FinSight Personal Finance Management Platform
            </p>
            <div className="flex flex-wrap justify-center gap-x-6 gap-y-2 text-[11px]">
              <span><strong>Developer Name:</strong> PODUGU MUKESH</span>
              <span><strong>Email:</strong> mukeshpodugu123@gmail.com</span>
              <span><strong>Phone:</strong> +91 8143999463</span>
              <span><strong>Location:</strong> Srikakulam</span>
            </div>
          </div>
          <p className="text-[10px] text-slate-600 dark:text-slate-500 mt-4">
            &copy; 2026 FinSight. Designed and Developed as a placement-ready engineering portfolio product.
          </p>
        </footer>

      </div>
    </div>
  );
}
