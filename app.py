from flask import Flask, request, render_template

import database_module as db

app = Flask(__name__)


@app.route('/')
def index():
    return render_template('Volunteer.html')


@app.route('/success', methods=['GET', 'POST'])
def upload():
    db.add_task(client_name=request.form['name'],
                address=request.form['address'],
                client_phone=request.form['phone'])

    return render_template('Success.html')


if __name__ == '__main__':
    app.run(host="0.0.0.0", debug=True, port=3434)


