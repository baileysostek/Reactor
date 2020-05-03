function fibonacci(){
    fib(1, 1);
}


function fib(par1, par2){
    console.log(par1);
    return fib(par2, par1 + par2);
}