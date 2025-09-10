import React from "react";
import "./components/pages/global.css";

import ReactDOM from "react-dom/client";
import Greeting from "./components/Greeting/Greeting";

const rootElement = document.getElementById("root");
if (!rootElement) throw new Error("Root element not found");

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <Greeting />
  </React.StrictMode>
);
