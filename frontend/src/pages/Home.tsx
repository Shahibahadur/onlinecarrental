import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import {
  Sparkles,
  Shield,
  Clock,
  ChevronDown,
  LayoutDashboard,
  CalendarCheck,
  CarFront,
  ArrowRight,
} from 'lucide-react';
import type { RootState } from '../store';
import { recommendationsAPI } from '../api/recommendations';
import type { Car } from '../types/car';
import CarCard from '../components/Car/CarCard';
import LoginModal from '../components/Auth/LoginModal';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);

  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [recommended, setRecommended] = useState<Car[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const displayName = useMemo(() => {
    const fn = user?.firstName?.trim();
    if (fn) return fn;
    const email = user?.email;
    if (email?.includes('@')) return email.split('@')[0];
    return 'there';
  }, [user]);

  const isAdmin = user?.role?.toUpperCase() === 'ADMIN';

  const handleGuestJoin = () => setIsLoginModalOpen(true);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      try {
        setError(null);
        setLoading(true);
        if (isAuthenticated) {
          try {
            const res = await recommendationsAPI.getPersonalizedRecommendations();
            if (!mounted) return;
            if (res.data && res.data.length > 0) {
              setRecommended(res.data);
            } else {
              const pop = await recommendationsAPI.getPopularCars();
              if (!mounted) return;
              setRecommended(pop.data || []);
            }
          } catch {
            const pop = await recommendationsAPI.getPopularCars();
            if (!mounted) return;
            setRecommended(pop.data || []);
          }
        } else {
          const pop = await recommendationsAPI.getPopularCars();
          if (!mounted) return;
          setRecommended(pop.data || []);
        }
      } catch (e: unknown) {
        if (!mounted) return;
        setError(e instanceof Error ? e.message : 'Failed to load recommendations');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [isAuthenticated]);

  return (
    <div className="min-h-screen relative overflow-x-hidden">
      <section className="relative min-h-[92vh] flex flex-col justify-center home-hero-gradient">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div
            className="home-blob absolute -top-24 -right-24 h-[420px] w-[420px] rounded-full bg-primary-600/25 blur-[100px]"
            aria-hidden
          />
          <div
            className="home-blob-delay absolute bottom-0 left-0 h-[380px] w-[380px] rounded-full bg-amber-500/15 blur-[90px]"
            aria-hidden
          />
          <div
            className="absolute top-1/2 left-1/2 h-[min(90vw,720px)] w-[min(90vw,720px)] -translate-x-1/2 -translate-y-1/2 rounded-full border border-white/[0.06]"
            aria-hidden
          />
        </div>

        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_50%_at_50%_-20%,rgba(234,88,12,0.18),transparent)]" />

        {/* Animated Hero Car */}
        <div className="absolute right-4 md:right-12 lg:right-24 top-1/3 transform -translate-y-1/2 hidden lg:block hero-animated-car z-5">
          <svg viewBox="0 0 400 200" className="w-80 md:w-96 lg:w-[480px] h-auto drop-shadow-2xl">
            {/* Car Body */}
            <path d="M50,140 L70,140 L80,90 L120,70 L280,70 L320,90 L330,140 L350,140 
                     L350,155 L335,155 L335,165 L45,165 L45,155 L30,155 L30,140 Z" 
                  fill="url(#carGradient)" 
                  stroke="#1a1a1a" 
                  strokeWidth="2"/>
            
            {/* Car Windows */}
            <path d="M115,78 L135,68 L200,68 L200,100 L115,100 Z" fill="#1e3a5f" opacity="0.8" stroke="#0f172a" strokeWidth="1.5"/>
            <path d="M210,68 L265,78 L285,100 L210,100 Z" fill="#1e3a5f" opacity="0.8" stroke="#0f172a" strokeWidth="1.5"/>
            
            {/* Windshield Glare */}
            <path d="M125,75 L180,75 L175,95 L130,95 Z" fill="rgba(255,255,255,0.2)"/>
            
            {/* Headlights */}
            <circle cx="338" cy="120" r="8" fill="#fef3c7" className="car-headlight"/>
            <circle cx="338" cy="120" r="5" fill="#fbbf24"/>
            <ellipse cx="360" cy="120" rx="40" ry="15" fill="url(#headlightGradient)" opacity="0.5"/>
            
            {/* Tail Lights */}
            <circle cx="42" cy="120" r="6" fill="#ef4444"/>
            <circle cx="42" cy="120" r="3" fill="#dc2626"/>
            
            {/* Grill */}
            <rect x="310" y="125" width="25" height="15" rx="2" fill="#1f2937" stroke="#374180" strokeWidth="1"/>
            <line x1="312" y1="128" x2="333" y2="128" stroke="#9ca3af" strokeWidth="1"/>
            <line x1="312" y1="132" x2="333" y2="132" stroke="#9ca3af" strokeWidth="1"/>
            <line x1="312" y1="136" x2="333" y2="136" stroke="#9ca3af" strokeWidth="1"/>
            
            {/* Front Wheel */}
            <g className="car-wheel" style={{ transformOrigin: '290px 150px' }}>
              <circle cx="290" cy="150" r="28" fill="#111827" stroke="#374151" strokeWidth="3"/>
              <circle cx="290" cy="150" r="20" fill="#1f2937"/>
              <circle cx="290" cy="150" r="10" fill="#4b5563"/>
              <line x1="290" y1="122" x2="290" y2="178" stroke="#6b7280" strokeWidth="2"/>
              <line x1="262" y1="150" x2="318" y2="150" stroke="#6b7280" strokeWidth="2"/>
              <line x1="270" y1="130" x2="310" y2="170" stroke="#6b7280" strokeWidth="2"/>
              <line x1="270" y1="170" x2="310" y2="130" stroke="#6b7280" strokeWidth="2"/>
            </g>
            
            {/* Back Wheel */}
            <g className="car-wheel" style={{ transformOrigin: '90px 150px' }}>
              <circle cx="90" cy="150" r="28" fill="#111827" stroke="#374151" strokeWidth="3"/>
              <circle cx="90" cy="150" r="20" fill="#1f2937"/>
              <circle cx="90" cy="150" r="10" fill="#4b5563"/>
              <line x1="90" y1="122" x2="90" y2="178" stroke="#6b7280" strokeWidth="2"/>
              <line x1="62" y1="150" x2="118" y2="150" stroke="#6b7280" strokeWidth="2"/>
              <line x1="70" y1="130" x2="110" y2="170" stroke="#6b7280" strokeWidth="2"/>
              <line x1="70" y1="170" x2="110" y2="130" stroke="#6b7280" strokeWidth="2"/>
            </g>
            
            {/* Door Handles */}
            <rect x="140" y="105" width="20" height="5" rx="2" fill="#374151"/>
            <rect x="240" y="105" width="20" height="5" rx="2" fill="#374151"/>
            
            {/* Side Mirror */}
            <rect x="295" y="85" width="15" height="10" rx="3" fill="#ea580c" stroke="#c2410c" strokeWidth="1"/>
            
            {/* Gradients */}
            <defs>
              <linearGradient id="carGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stopColor="#f97316"/>
                <stop offset="50%" stopColor="#ea580c"/>
                <stop offset="100%" stopColor="#c2410c"/>
              </linearGradient>
              <linearGradient id="headlightGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="#fef3c7" stopOpacity="0.8"/>
                <stop offset="100%" stopColor="#fef3c7" stopOpacity="0"/>
              </linearGradient>
            </defs>
          </svg>
          
          {/* Car shadow */}
          <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-64 h-4 bg-black/20 rounded-full blur-xl"></div>
        </div>

        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full py-20 md:py-28">
          {isAuthenticated ? (
            <div className="max-w-4xl">
              <div className="home-stagger-1 inline-flex items-center gap-2 rounded-full border border-emerald-400/30 bg-emerald-500/10 px-4 py-1.5 text-sm text-emerald-100 backdrop-blur-md">
                <Sparkles className="h-4 w-4 text-emerald-300" aria-hidden />
                <span className="font-medium tracking-wide">
                  Signed in — recommendations and shortcuts are personalized for you
                </span>
              </div>

              <h1
                className="home-stagger-2 mt-8 text-5xl sm:text-6xl md:text-7xl lg:text-8xl font-extrabold leading-[1.05] tracking-tight"
                style={{ fontFamily: 'var(--font-family-display), ui-sans-serif, system-ui' }}
              >
                <span className="text-white drop-shadow-sm">Welcome back,</span>
                <br />
                <span className="bg-gradient-to-r from-amber-200 via-primary-400 to-orange-300 bg-clip-text text-transparent">
                  {displayName}
                </span>
              </h1>

              <p className="home-stagger-3 mt-8 text-lg md:text-xl text-white/85 max-w-2xl leading-relaxed">
                Here&apos;s what we&apos;ve lined up based on your preferences and activity. Jump to your
                dashboard, check reservations, or keep browsing the fleet.
              </p>

              <div className="home-stagger-3 mt-10 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {isAdmin ? (
                  <Link
                    to="/admin"
                    className="group flex items-center justify-between gap-3 rounded-2xl border border-white/20 bg-white/10 px-5 py-4 text-white backdrop-blur-md transition hover:border-white/35 hover:bg-white/15"
                  >
                    <span className="flex items-center gap-3 font-semibold">
                      <LayoutDashboard className="h-5 w-5 text-amber-300" />
                      Admin panel
                    </span>
                    <ArrowRight className="h-4 w-4 opacity-70 transition group-hover:translate-x-0.5" />
                  </Link>
                ) : (
                  <>
                    <Link
                      to="/dashboard"
                      className="group flex items-center justify-between gap-3 rounded-2xl border border-white/20 bg-white/10 px-5 py-4 text-white backdrop-blur-md transition hover:border-white/35 hover:bg-white/15"
                    >
                      <span className="flex items-center gap-3 font-semibold">
                        <LayoutDashboard className="h-5 w-5 text-amber-300" />
                        Dashboard
                      </span>
                      <ArrowRight className="h-4 w-4 opacity-70 transition group-hover:translate-x-0.5" />
                    </Link>
                    <Link
                      to="/my-reservations"
                      className="group flex items-center justify-between gap-3 rounded-2xl border border-white/20 bg-white/10 px-5 py-4 text-white backdrop-blur-md transition hover:border-white/35 hover:bg-white/15"
                    >
                      <span className="flex items-center gap-3 font-semibold">
                        <CalendarCheck className="h-5 w-5 text-sky-300" />
                        My reservations
                      </span>
                      <ArrowRight className="h-4 w-4 opacity-70 transition group-hover:translate-x-0.5" />
                    </Link>
                  </>
                )}
                <Link
                  to="/cars-grid"
                  className="group flex items-center justify-between gap-3 rounded-2xl border border-white/20 bg-white/10 px-5 py-4 text-white backdrop-blur-md transition hover:border-white/35 hover:bg-white/15 sm:col-span-2 lg:col-span-1"
                >
                  <span className="flex items-center gap-3 font-semibold">
                    <CarFront className="h-5 w-5 text-orange-200" />
                    Browse all cars
                  </span>
                  <ArrowRight className="h-4 w-4 opacity-70 transition group-hover:translate-x-0.5" />
                </Link>
              </div>

              <ul className="home-stagger-4 mt-12 flex flex-wrap gap-8 text-sm text-white/75">
                <li className="flex items-center gap-2">
                  <Shield className="h-5 w-5 shrink-0 text-emerald-400/90" aria-hidden />
                  <span>Your account is secure</span>
                </li>
                <li className="flex items-center gap-2">
                  <Sparkles className="h-5 w-5 shrink-0 text-amber-300/90" aria-hidden />
                  <span>Picks tailored to you</span>
                </li>
                <li className="flex items-center gap-2">
                  <Clock className="h-5 w-5 shrink-0 text-sky-300/90" aria-hidden />
                  <span>Book again in seconds</span>
                </li>
              </ul>
            </div>
          ) : (
            <div className="max-w-3xl">
              <div className="home-stagger-1 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-4 py-1.5 text-sm text-white/90 backdrop-blur-md">
                <Sparkles className="h-4 w-4 text-amber-400" aria-hidden />
                <span className="font-medium tracking-wide">Drive anywhere. Arrive in style.</span>
              </div>

              <h1
                className="home-stagger-2 mt-8 text-5xl sm:text-6xl md:text-7xl lg:text-8xl font-extrabold leading-[1.05] tracking-tight"
                style={{ fontFamily: 'var(--font-family-display), ui-sans-serif, system-ui' }}
              >
                <span className="text-white drop-shadow-sm">Rent your</span>
                <br />
                <span className="bg-gradient-to-r from-amber-200 via-primary-400 to-orange-300 bg-clip-text text-transparent">
                  dream ride
                </span>
              </h1>

              <p className="home-stagger-3 mt-8 text-lg md:text-xl text-white/85 max-w-xl leading-relaxed">
                Curated vehicles, transparent pricing, and a booking flow built for real trips — from
                city runs to weekend getaways with family and friends.
              </p>

              <div className="home-stagger-3 mt-10 flex flex-wrap gap-4">
                <button
                  type="button"
                  onClick={handleGuestJoin}
                  className="group relative overflow-hidden rounded-2xl bg-gradient-to-r from-primary-600 to-orange-600 px-8 py-4 text-base font-semibold uppercase tracking-wider text-white shadow-lg shadow-primary-900/40 transition hover:scale-[1.02] hover:shadow-xl active:scale-[0.98]"
                >
                  <span className="relative z-10">Join us</span>
                  <span
                    className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/25 to-transparent transition duration-700 group-hover:translate-x-full"
                    aria-hidden
                  />
                </button>
                <Link
                  to="/cars-grid"
                  className="rounded-2xl border border-white/25 bg-white/10 px-8 py-4 text-center text-base font-semibold uppercase tracking-wider text-white backdrop-blur-md transition hover:border-white/40 hover:bg-white/15"
                >
                  Browse cars
                </Link>
              </div>

              <ul className="home-stagger-4 mt-14 flex flex-wrap gap-8 text-sm text-white/75">
                <li className="flex items-center gap-2">
                  <Shield className="h-5 w-5 shrink-0 text-emerald-400/90" aria-hidden />
                  <span>Verified fleet</span>
                </li>
                <li className="flex items-center gap-2">
                  <Clock className="h-5 w-5 shrink-0 text-sky-300/90" aria-hidden />
                  <span>Quick booking</span>
                </li>
                <li className="flex items-center gap-2">
                  <Sparkles className="h-5 w-5 shrink-0 text-amber-300/90" aria-hidden />
                  <span>Picks matched to you</span>
                </li>
              </ul>
            </div>
          )}
        </div>

        {/* Animated Road */}
        <div className="hero-road absolute bottom-0 left-0 right-0" />

        <div className="relative z-10 flex justify-center pb-10">
          <span
            className="inline-flex animate-[bounce-soft_2.2s_ease-in-out_infinite] text-white/50"
            aria-hidden
          >
            <ChevronDown className="h-8 w-8" />
          </span>
        </div>
      </section>

      <section className="relative border-t border-neutral-200/80 bg-gradient-to-b from-neutral-50 to-white">
        <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-primary-300/50 to-transparent" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-20">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between mb-10">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-primary-600">
                {isAuthenticated ? 'For you' : 'Trending'}
              </p>
              <h2
                className="mt-2 text-3xl md:text-4xl font-bold text-neutral-900"
                style={{ fontFamily: 'var(--font-family-display), sans-serif' }}
              >
                {isAuthenticated ? 'Recommended for you' : 'Popular cars'}
              </h2>
              <p className="mt-2 text-neutral-600 max-w-lg">
                {isAuthenticated
                  ? 'Based on your rentals, views, and preferences — we blend personalized picks with popular choices when needed.'
                  : 'What renters are booking right now. Sign in for suggestions tailored to you.'}
              </p>
            </div>
            <button
              type="button"
              onClick={() => navigate('/cars-grid')}
              className="self-start sm:self-auto rounded-xl border border-neutral-200 bg-white px-5 py-2.5 text-sm font-semibold text-primary-700 shadow-sm transition hover:border-primary-300 hover:shadow-md"
            >
              View all
            </button>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div
                  key={i}
                  className="bg-white rounded-2xl shadow-sm border border-neutral-100 overflow-hidden"
                >
                  <div className="h-56 bg-gradient-to-br from-neutral-100 to-neutral-200 animate-pulse" />
                  <div className="p-4 space-y-3">
                    <div className="h-4 bg-neutral-200 rounded animate-pulse" />
                    <div className="h-4 bg-neutral-200 rounded animate-pulse w-2/3" />
                    <div className="h-10 bg-neutral-200 rounded animate-pulse" />
                  </div>
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="text-center text-red-600 py-12 rounded-2xl bg-red-50 border border-red-100">
              {error}
            </div>
          ) : recommended.length === 0 ? (
            <div className="text-center py-16 text-neutral-600 rounded-2xl border border-dashed border-neutral-200 bg-neutral-50/50">
              No cars to show right now.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
              {recommended.map((car, index) => (
                <div
                  key={car.id}
                  className="home-card-reveal"
                  style={{ animationDelay: `${Math.min(index, 8) * 0.06}s` }}
                >
                  <div className="h-full transition duration-300 hover:-translate-y-1">
                    <CarCard
                      car={car}
                      onBook={() => navigate(`/cars/${car.id}`)}
                      onViewDetails={() => navigate(`/cars/${car.id}`)}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {!isAuthenticated && (
        <LoginModal isOpen={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)} />
      )}
    </div>
  );
};

export default Home;
