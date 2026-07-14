import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

import { useAuthStore } from '../store/useAuthStore';
import { login as loginApi, signup as signupApi } from '../services/authService';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import logo from '../assets/redmeiIcon.png';

// Validation Schemas
const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

const signupSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;
type SignupFormValues = z.infer<typeof signupSchema>;

// Helper to decode email from JWT payload
function getEmailFromToken(token: string): string | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    return payload.sub || null;
  } catch (error) {
    return null;
  }
}

export const LoginPage = () => {
  const [isLoginView, setIsLoginView] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const setCredentials = useAuthStore((state) => state.setCredentials);
  const navigate = useNavigate();

  // Forms Hook Setup
  const {
    register: registerLogin,
    handleSubmit: handleLoginSubmit,
    formState: { errors: loginErrors },
    reset: resetLoginForm,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const {
    register: registerSignup,
    handleSubmit: handleSignupSubmit,
    formState: { errors: signupErrors },
    reset: resetSignupForm,
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
  });

  // Login handler
  const onLogin = async (data: LoginFormValues) => {
    setIsLoading(true);
    try {
      const response = await loginApi(data);
      if (response.success && response.data) {
        const email = getEmailFromToken(response.data.accessToken) || data.email;
        const name = email.split('@')[0];
        
        setCredentials(
          { id: '', email, name },
          response.data.accessToken,
          response.data.refreshToken
        );
        toast.success('Logged in successfully!');
        navigate('/dashboard');
      } else {
        toast.error(response.message || 'Authentication failed');
      }
    } catch (error: any) {
      const errMsg = error?.response?.data?.message || 'An unexpected error occurred';
      toast.error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  // Signup handler (with auto-login flow)
  const onSignup = async (data: SignupFormValues) => {
    setIsLoading(true);
    try {
      const response = await signupApi(data);
      if (response.success) {
        toast.success('Account created! Logging in...');
        
        // Auto-login flow
        const loginResponse = await loginApi({
          email: data.email,
          password: data.password,
        });

        if (loginResponse.success && loginResponse.data) {
          setCredentials(
            { id: response.data?.userId || '', email: data.email, name: data.name },
            loginResponse.data.accessToken,
            loginResponse.data.refreshToken
          );
          toast.success('Session started!');
          navigate('/dashboard');
        } else {
          toast.success('Registration completed! Please sign in.');
          setIsLoginView(true);
          resetLoginForm();
        }
      } else {
        toast.error(response.message || 'Registration failed');
      }
    } catch (error: any) {
      const errMsg = error?.response?.data?.message || 'An unexpected error occurred';
      toast.error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  // View toggle
  const toggleView = () => {
    setIsLoginView(!isLoginView);
    resetLoginForm();
    resetSignupForm();
  };

  return (
    <div className="flex min-h-screen w-full flex-col md:flex-row bg-background">
      {/* Left Pane (Brand/Marketing) */}
      <div className="flex md:w-1/2 flex-col justify-between p-8 md:p-16 bg-[#0F1011] border-b md:border-b-0 md:border-r border-subtle relative overflow-hidden">
        {/* Subtle decorative grid overlay from global CSS */}
        <div className="absolute inset-0 opacity-10 pointer-events-none" />
        
        {/* Brand Header */}
        <div className="flex items-center gap-3 relative z-10">
          <img src={logo} alt="readme.ai Logo" className="w-10 h-10 object-contain" />
          <span className="text-xl font-bold tracking-tight text-primary">readme.ai</span>
        </div>

        {/* Marketing Slogan */}
        <div className="my-auto py-12 md:py-0 relative z-10 max-w-lg">
          <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight mb-4 leading-tight">
            Unifying <span className="text-gradient">codebase intelligence</span>.
          </h1>
          <p className="text-secondary text-base leading-relaxed">
            Ingest repositories seamlessly, parse structural AST definitions, cache semantic contextual prompts, and execute conversational chat streams.
          </p>
        </div>

        {/* Mock Statistics */}
        <div className="grid grid-cols-3 gap-4 border-t border-subtle pt-8 relative z-10">
          <div>
            <div className="text-xl md:text-2xl font-bold text-cyan">100%</div>
            <div className="text-xs text-secondary mt-1">Vectorized AST</div>
          </div>
          <div>
            <div className="text-xl md:text-2xl font-bold text-green">Real-time</div>
            <div className="text-xs text-secondary mt-1">Semantic Search</div>
          </div>
          <div>
            <div className="text-xl md:text-2xl font-bold text-primary">Streamed</div>
            <div className="text-xs text-secondary mt-1">LLM Responses</div>
          </div>
        </div>
      </div>

      {/* Right Pane (Auth Form) */}
      <div className="flex md:w-1/2 items-center justify-center p-8 md:p-16 relative">
        <div className="glow-wrapper max-w-md w-full">
          <div className="surface-card p-8 md:p-10 flex flex-col gap-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-primary">
                {isLoginView ? 'Welcome Back' : 'Create Account'}
              </h2>
              <p className="text-sm text-secondary mt-1.5">
                {isLoginView
                  ? 'Sign in to access your codebase workspaces.'
                  : 'Get started with codebase intelligence.'}
              </p>
            </div>

            {isLoginView ? (
              /* Login Form */
              <form onSubmit={handleLoginSubmit(onLogin)} className="flex flex-col gap-4">
                <Input
                  label="Email Address"
                  type="email"
                  placeholder="name@company.com"
                  error={loginErrors.email?.message}
                  {...registerLogin('email')}
                />
                <Input
                  label="Password"
                  type="password"
                  placeholder="••••••••"
                  error={loginErrors.password?.message}
                  {...registerLogin('password')}
                />
                <Button type="submit" className="mt-2" isLoading={isLoading}>
                  Sign In
                </Button>
              </form>
            ) : (
              /* Signup Form */
              <form onSubmit={handleSignupSubmit(onSignup)} className="flex flex-col gap-4">
                <Input
                  label="Full Name"
                  type="text"
                  placeholder="Jane Doe"
                  error={signupErrors.name?.message}
                  {...registerSignup('name')}
                />
                <Input
                  label="Email Address"
                  type="email"
                  placeholder="name@company.com"
                  error={signupErrors.email?.message}
                  {...registerSignup('email')}
                />
                <Input
                  label="Password"
                  type="password"
                  placeholder="•••••••• (Min 6 characters)"
                  error={signupErrors.password?.message}
                  {...registerSignup('password')}
                />
                <Button type="submit" className="mt-2" isLoading={isLoading}>
                  Create Account
                </Button>
              </form>
            )}

            {/* Toggle Link */}
            <div className="text-center text-sm text-secondary border-t border-subtle pt-5">
              {isLoginView ? "Don't have an account?" : 'Already have an account?'}{' '}
              <button
                type="button"
                onClick={toggleView}
                className="text-cyan hover:underline font-semibold ml-1 cursor-pointer focus:outline-none"
              >
                {isLoginView ? 'Create Account' : 'Sign In'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
