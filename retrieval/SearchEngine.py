# coding=utf-8
from flask import Flask
from flask import request
from flask import render_template
import codecs

import os

app = Flask(__name__)


@app.route('/')
def search():
    #if request.form['submit'] == 'find':
        #return render_template('index2.html')
    return render_template('index.html')

@app.route('/',methods=['POST'])
def event():
    temp=request.form['search']

    os.system("java -jar test.jar "+temp+" > result.txt")
    with codecs.open("result.txt", 'r') as f:
        return f.readlines()[0]






if __name__ == '__main__':
    app.run()