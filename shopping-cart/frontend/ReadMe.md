# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.




---

Quick start (project-specific)

1. Install dependencies

```bash
cd frontend
npm install
```

2. Run development server (proxies `/api` to backend at http://localhost:8080)

```bash
npm run dev
```

3. Build for production

```bash
npm run build
```

Notes:
- Backend must be running at `http://localhost:8080` for the Vite proxy to forward API requests.
- The frontend uses relative `/api/...` endpoints so it works with proxy or when deployed behind the same origin as the backend.



