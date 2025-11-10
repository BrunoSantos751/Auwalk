import { render, screen } from "@testing-library/react";
import { AsideNav } from "./../testes/aside-nav.spec.ts";

describe("AsideNav", () => {
  it(" Should render correctly", () => {
    render(<AsideNav />);

    // verifica se o texto 'All files' aparece na tela
    expect(screen.getByText("All files")).toBeInTheDocument();
  });
});
