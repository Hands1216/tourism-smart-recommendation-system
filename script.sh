cd "c:/Users/Ghostxx H/Desktop/毕设"
rm -rf .git
echo "target/" > .gitignore
echo "**/application*.yml" >> .gitignore
git init
git add .
git commit -m "Initial commit (clean)"
git remote add origin https://github.com/Hands1216/tourism-smart-recommendation-system.git
git push -u origin master --force