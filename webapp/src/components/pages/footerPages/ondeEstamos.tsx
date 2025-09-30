import "./pagesfooter.css";

const OndeEstamos: React.FC = () => {
  return (
    <div className="dados-conteiner-footer-pages">
      <h2 className="pages-h2">Onde Estamos</h2>

      <p className="pages-p">
        Atualmente,{" "}
        <span className="p-destaque">
          {" "}
          nosso foco inicial é em Maceió, Alagoas{" "}
        </span>
        , oferecendo acesso a serviços de qualidade por meio da nossa plataforma
        digital, que estará disponível em{" "}
        <span className="p-destaque"> aplicativo móvel e web. </span>
        <hr /> Dessa forma, tutores e prestadores podem se conectar facilmente,
        fortalecendo a oferta local enquanto expandimos gradualmente para outras
        regiões do Brasil.
      </p>
    </div>
  );
};

export default OndeEstamos;
