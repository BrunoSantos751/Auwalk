import "./pagesfooter.css";

const Ajuda: React.FC = () => {
  return (
    <div className="dados-conteiner-footer-pages">
      <h2 className="pages-h2">Ajuda</h2>

      <p className="pages-p">
        Na seção de Ajuda, o usuário encontra respostas para as dúvidas mais
        comuns sobre cadastro, agendamento, pagamentos e acompanhamento de
        serviços. Além disso, disponibilizamos canais de contato para suporte
        direto, garantindo atendimento rápido e transparente.
        <p>
          Se precisar de ajuda, você pode entrar em contato conosco pelo e-mail:{" "}
          <a
            href="mailto:auwalksuporte@gmail.com?subject=Preciso%20de%20ajuda"
            className="email-link"
          >
            <span className="p-destaque"> auwalksuporte@gmail.com </span>
          </a>
        </p>
      </p>
    </div>
  );
};

export default Ajuda;
