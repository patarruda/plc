function ataque(atacante, defensor) -- Função de batalha, para calcular o dano causado por uma taque
    local dano = 0
    local numeroAtaque = math.random(20) -- Utilizando a função "random" para gerar um valor aleatório
    if numeroAtaque <= 10 then
      dano = 50
      print(atacante.." utilizou o eletrizante Choque do Trovao e causou 50 de dano")
    elseif numeroAtaque >= 11  and numeroAtaque <= 15 then
      dano = 100
      print(atacante.." utilizou sua potente Calda de ferro e causou 100 de dano")
    elseif numeroAtaque >= 16 and numeroAtaque <= 18 then
      dano = 150
      print(atacante.." utilizou a famosa Investida Trovao e causou 150 de dano")
    else
      dano = 200
      print(atacante.." utilizou o seu poderoso Trovao e causou 200 de dano")
    end
    defensor[1] = defensor[1] - dano
  end
  
  function batalha()
    local pikachu = {800}
    local raichu = {1000}
    local atacante = "Pikachu"
    while pikachu[1] > 0 and raichu[1] > 0 do
      coroutine.yield()
      if atacante == "Pikachu" then
        ataque("Pikachu", raichu)
        atacante = "Raichu"
      else
        ataque("Raichu", pikachu)
        atacante = "Pikachu"
      end
      print("Pikachu: "..pikachu[1].." HP")
      print("Raichu: "..raichu[1].." HP")
    end
    if pikachu[1] <= 0 then
      print("Raichu ganhou!")
    else
      print("Pikachu ganhou!")
    end
  end
  
  batalhaCoroutine = coroutine.create(batalha)
  
  while coroutine.status(batalhaCoroutine) ~= "dead" do
    coroutine.resume(batalhaCoroutine)
  end