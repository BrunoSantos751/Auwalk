import "./pagesfooter.css";

const ComoFunciona: React.FC = () => {
  return (
    <div className="dados-conteiner-footer-pages">
      <h2 className="pages-h2">Como Funciona</h2>

      <p className="pages-p">
        Nossa plataforma conecta{" "}
        <span className="p-destaque">
          tutores de animais de estimação a prestadores de serviços de confiança
        </span>
        , como passeadores e pet sitters. De forma simples e segura, o usuário
        realiza o cadastro, escolhe o serviço desejado, agenda o atendimento e
        acompanha todas as atividades em tempo real.
        <hr />
        <span className="p-destaque">
          O objetivo é garantir praticidade para os tutores e qualidade no
          cuidado com os pets.
        </span>
      </p>
    </div>
  );
};

export default ComoFunciona;
