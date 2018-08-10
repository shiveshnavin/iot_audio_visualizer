load('api_gpio.js');
load('api_pwm.js') ; 
load('api_rpc.js') ; 
load('api_sys.js') ; 

let red = 4;
let green = 15;
let green2 = 16;
let blue = 5;
let white = 19;
let l = {"h":0,"s":0,"v":0};
let s = JSON.stringify(l);
let n = JSON.parse(s);

l =  {"h":0,"s":0,"v":0};
s = JSON.stringify(l);
let prevColor = JSON.parse(s);


let checkHSV=function(h,s,v){
print('h ',h,' s ',s,' v ',v);
if(h<0 || h>360 || h===undefined){
    return false;
}
if(s<0 || s>1 || s===undefined){
    return false;
}
if(v<0 || v>1 || v===undefined){
    return false;
}
return true;
};

let getRGB = function(h,s,v){
if(h<0){
    h=0;
}
if(h>360){
    h=360;
}
if(s<0){
    s=0;
}
if(s>1){
    s=1;
}
if(v<0){
    v=0;
}
if(v>1){
    v=1;
}

let getr= ffi('int convertHSI2RGB_R(float , float , float)');
let getg= ffi('int convertHSI2RGB_G(float , float , float)');
let getb= ffi('int convertHSI2RGB_B(float , float , float)');
let getw= ffi('int convertHSI2RGB_W(float , float , float)');
// print("301 H ",h,"S ",s,"V ",v);
return {
    r:getr(h,s,v),
    g:getg(h,s,v),
    b:getb(h,s,v),
    w:getw(h,s,v)
};
};

let color = function(prev_hsv,args_hsv){
let r,g,b,w=0;
let args=getRGB(args_hsv.h,args_hsv.s,args_hsv.v);
let prev=getRGB(prev_hsv.h,prev_hsv.s,prev_hsv.v);
       
r = args.r - prev.r;
g = args.g - prev.g;
b = args.b - prev.b;
w = args.w - prev.w;
// print("58 r ",r,"g ",g,"b ",b,"w ",w);

animate(prev,r,g,b,w);		
return  {"h":args_hsv.h,"s":args_hsv.s,"v":args_hsv.v};
};

let step=5;
let animate = function(prev,r,g,b,w){
for(let i=0 ; i<=step;i++){
    let pp = {r:0,g:0,b:0,w:0};
    pp.r = prev.r + ((r * i)/step);
    pp.g = prev.g + ((g * i)/step);
    pp.b = prev.b + ((b * i)/step);
    pp.w = prev.w + ((w * i)/step);
    //print("58 r ",pp.r,"g ",pp.g,"b ",pp.b,"w ",pp.w);
   
    PWM.set(red,1000,(pp.r/255));
    PWM.set(green,1000,(pp.g/255));
    PWM.set(green2,1000,(pp.g/255));
    PWM.set(blue,1000,(pp.b/255));
    PWM.set(white,1000,(pp.w/255));
    Sys.usleep(5);
    n =   {r:0,g:0,b:0,w:0};
    n = pp;
}
return n;
};

RPC.addHandler('color',function(args){
    print('params ',JSON.stringify(args));
    if(!checkHSV(args.h,args.s,args.v))
    {
        return {result:"Incorrect HSV values"};
    }
	 
	prevColor = color(prevColor,args);
    return {result:"success"};
});
