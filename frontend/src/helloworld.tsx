export default function HelloWorld() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 text-white p-4">
      <div className="bg-white/10 backdrop-blur-md rounded-2xl p-8 shadow-2xl border border-white/20 text-center max-w-md w-full transform hover:scale-105 transition-transform duration-300">
        <h1 className="text-4xl font-extrabold tracking-tight mb-4 drop-shadow-md">
          Hello, World!
        </h1>
        <p className="text-lg text-indigo-100 mb-6">
          Everything is working perfectly. React, React Router, Tailwind CSS, and your other dependencies are ready to go!
        </p>
        <div className="inline-block bg-white text-indigo-600 px-6 py-2.5 rounded-full font-semibold shadow-md hover:bg-indigo-50 transition-colors cursor-pointer">
          Get Started
        </div>
      </div>
    </div>
  );
}
