const readline = require('readline')
const fs = require('fs')
const path = require('path')

let rs = fs.createReadStream(path.join(__dirname, 'format.txt'))
let ws = fs.createWriteStream(path.join(__dirname, 'output.txt'))

const rl = readline.createInterface({
  input: rs,
  output: ws
})

rl.on('line', (line) => {
  console.log(`${line}`)
  if (line !== '') {
    if (line[0] === 'M' || line[0] === 'Y' || line[0] === 'X') {
      line = line.substr(1, line.length)
      line = 'LB' + line
      ws.write(line + '\r\n')
    } else if (line[0] === 'D') {
      line = line.substr(1, line.length)
      line = 'LW' + line
      ws.write(line + '\r\n')
    }
  } else {
    ws.write('\r\n')
  }
})
rl.on('close', () => {
  console.log('closed.')
})