## Projet final de Compilation
Utilisation de processing pour afficher les résultats.

Les fichiers doivent se trouver dans le dossier data à la racine de lancement du projet.

Si le text est trop long, il est possible de faire défilé l'écran avec la
molette de la souris.

Etant donnée que les fichiers sont lu dans la méthode draw (au détriment des performance ou de votre ordinateur), il est 
possible de modifier en live le fichier pour voir les erreurs.

ex:si le projet lancé dans rep `/user/test/projetCompil`  
les fichiers devront être dans `/user/test/projetCompil/data`

les fichiers nécéssaires sont:
- `keywords.txt` qui contient les mots clé du langage séparé par des espaces.
- `rules.txt` qui contient la grammaire. chaque ligne doit être numérotée et les colonnes séparées par des tabulations.
- `dictionnary.csv` qui contient la table du langage.

Les fichiers de tests doivent commencer par 'test' et avoir l'extension '.txt'.  
Il est possible d'en créer autant que vous le voulez en plus de ceux existants. Pour les avoir dans la fenêtre, il suffira de reset le compilateur.

commande run: `maven compile exec:java -Dfile.encoding=utf-8 -Dexec.mainClass=Main`
commande jar: `maven clean compile assembly:single`