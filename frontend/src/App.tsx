import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HelloWorld from './helloworld';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HelloWorld />} />
      </Routes>
    </BrowserRouter>
  );
}
