          % processing function definition: 
evaluate0 sw -4(r14),r15
          % processing: t1 := 1
          addi r1,r0,1
          sw -20(r14),r1
          % processing: AssignStatNode(bb=1;)
          % processing: bb := t1
          lw r2,-20(r14)
          sw -16(r14),r2
          % processing: t2 := bb+x
          lw r1,-16(r14)
          lw r4,-12(r14)
          add  r5,r1,r4
          sw -24(r14),r5
          % processing: return(return(bb+x);)
          lw r3,-24(r14)
          sw 0(r14),r3
          lw r15,-4(r14)
          jr r15
          % processing function definition: 
evaluate1 sw -4(r14),r15
          % processing: t3 := 1
          addi r3,r0,1
          sw -20(r14),r3
          % processing: AssignStatNode(bb=1;)
          % processing: bb := t3
          lw r5,-20(r14)
          sw -16(r14),r5
          lw r15,-4(r14)
          jr r15
          % processing function definition: 
freefunc2 sw -4(r14),r15
          % processing: t4 := 1
          addi r4,r0,1
          sw -16(r14),r4
          % processing: AssignStatNode(input=1;)
          % processing: input := t4
          lw r5,-16(r14)
          sw -12(r14),r5
          % processing: t5 := input+x
          lw r5,-12(r14)
          lw r4,-8(r14)
          add  r1,r5,r4
          sw -20(r14),r1
          % processing: put(t5)
          lw r1,-20(r14)
          % put value on stack
          addi r14,r14,-20
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-20
          % put value on stack
          addi r4,r0,space
          addi r14,r14,-20
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-20
          lw r15,-4(r14)
          jr r15
          entry
          addi r14,r0,topaddr
          % processing: t6 := 1
          addi r4,r0,1
          sw -1068(r14),r4
          % processing: AssignStatNode(polynomial1.b=1;)
          % processing:  := t6
          lw r1,-1068(r14)
          sw -4(r14),r1
          % processing: t7 := 1
          addi r5,r0,1
          sw -1072(r14),r5
          % processing: AssignStatNode(polynomial2.d=1;)
          % processing:  := t7
          lw r1,-1072(r14)
          sw -56(r14),r1
          % processing: t8 := 1
          addi r4,r0,1
          sw -1076(r14),r4
          % processing: AssignStatNode(polynomial2.b=1;)
          % processing:  := t8
          lw r1,-1076(r14)
          sw -52(r14),r1
          % processing: t9 := 1
          addi r5,r0,1
          sw -1080(r14),r5
          % processing: function call to linear1.polynomial_obj.evaluate(1) 
          lw r5,-1080(r14)
          sw -1272(r14),r5
          addi r14,r14,-1260
          jl r15,evaluate0
          subi r14,r14,-1260
          lw r5,-1260(r14)
          sw -1084(r14),r5
          % processing: put(t10)
          lw r5,-1084(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-1260
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t12 := 9
          addi r1,r0,9
          sw -1092(r14),r1
          % processing: AssignStatNode(polynomial1.array[1]=9;)
          % processing:  := t12
          lw r5,-1092(r14)
          sw -88(r14),r5
          % processing: put()
          lw r4,-88(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r4
          % link buffer to stack
          addi r4,r0, buf
          sw -12(r14),r4
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-1260
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-1260
          % processing: put()
          lw r5,-4(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r4,r0,space
          addi r14,r14,-1260
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t14 := 2
          addi r4,r0,2
          sw -1100(r14),r4
          % processing: AssignStatNode(linear1.polynomial_obj.b=2;)
          % processing:  := t14
          lw r5,-1100(r14)
          sw -108(r14),r5
          % processing: AssignStatNode(b=linear1.polynomial_obj.b;)
          % processing: b := 
          lw r5,-108(r14)
          sw -160(r14),r5
          % processing: put(b)
          lw r4,-160(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r4
          % link buffer to stack
          addi r4,r0, buf
          sw -12(r14),r4
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-1260
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t15 := polynomial1.b+polynomial2.b
          lw r4,-4(r14)
          lw r1,-52(r14)
          add  r3,r4,r1
          sw -1104(r14),r3
          % processing: AssignStatNode(c=polynomial1.b+polynomial2.b;)
          % processing: c := t15
          lw r1,-1104(r14)
          sw -164(r14),r1
          % processing: function call to polynomial1.evaluate(c) 
          lw r4,-164(r14)
          sw -1272(r14),r4
          addi r14,r14,-1260
          jl r15,evaluate0
          subi r14,r14,-1260
          lw r4,-1260(r14)
          sw -1108(r14),r4
          % processing: AssignStatNode(result=polynomial1.evaluate(c);)
          % processing: result := t16
          lw r1,-1108(r14)
          sw -172(r14),r1
          % processing: put(result)
          lw r3,-172(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r3
          % link buffer to stack
          addi r3,r0, buf
          sw -12(r14),r3
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-1260
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t19 := 4
          addi r1,r0,4
          sw -1120(r14),r1
          % processing: AssignStatNode(array[1][1]=4;)
          % processing:  := t19
          lw r3,-1120(r14)
          sw -4576(r14),r3
          % processing: t22 := 1
          addi r4,r0,1
          sw -1132(r14),r4
          % processing: t23 := array[1][1]+1
          lw r3,-4576(r14)
          lw r1,-1132(r14)
          add  r5,r3,r1
          sw -1136(r14),r5
          % processing: put(t23)
          lw r5,-1136(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-1260
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t25 := 5
          addi r1,r0,5
          sw -1144(r14),r1
          % processing: AssignStatNode(array_object[2]=5;)
          % processing:  := t25
          lw r5,-1144(r14)
          sw -1548(r14),r5
          % processing: put()
          lw r3,-1548(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r3
          % link buffer to stack
          addi r3,r0, buf
          sw -12(r14),r3
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-1260
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t27 := 4
          addi r5,r0,4
          sw -1152(r14),r5
          % processing: t28 := 3
          addi r5,r0,3
          sw -1156(r14),r5
          % processing: t29 := 4*3
          lw r3,-1152(r14)
          lw r1,-1156(r14)
          mul  r4,r3,r1
          sw -1160(r14),r4
          % processing: t30 := 4
          addi r4,r0,4
          sw -1164(r14),r4
          % processing: t31 := 100
          addi r4,r0,100
          sw -1168(r14),r4
          % processing: t32 := 4+100
          lw r1,-1164(r14)
          lw r3,-1168(r14)
          add  r5,r1,r3
          sw -1172(r14),r5
          % processing: t33 := 1
          addi r5,r0,1
          sw -1176(r14),r5
          % processing: t34 := 1
          addi r5,r0,1
          sw -1180(r14),r5
          % processing: t35 := 1+1
          lw r3,-1176(r14)
          lw r1,-1180(r14)
          add  r4,r3,r1
          sw -1184(r14),r4
          % processing: t36 := 4+100/1+1
          lw r1,-1172(r14)
          lw r3,-1184(r14)
          div  r5,r1,r3
          sw -1188(r14),r5
          % processing: t37 := 4*3+4+100/1+1
          lw r3,-1160(r14)
          lw r1,-1188(r14)
          add  r4,r3,r1
          sw -1192(r14),r4
          % processing: t38 := 58
          addi r4,r0,58
          sw -1196(r14),r4
          % processing: t39 := 4*3+4+100/1+1-58
          lw r1,-1192(r14)
          lw r3,-1196(r14)
          sub  r5,r1,r3
          sw -1200(r14),r5
          % processing: AssignStatNode(express_test=4*3+4+100/1+1-58;)
          % processing: express_test := t39
          lw r3,-1200(r14)
          sw -176(r14),r3
          % processing: put(express_test)
          lw r1,-176(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r3,r0,space
          addi r14,r14,-1260
          sw -8(r14),r3
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r3,r0,input
          addi r14,r14,-1260
          sw -8(r14),r3
          jl r15, putstr
          subi r14,r14,-1260
          % processing: read(read_test)
          addi r3, r0, buf
          sw -8(r14),r3
          jl r15,getstr
          jl r15,strint
          sw -584(r14), r13
          % processing: t40 := 2
          addi r1,r0,2
          sw -1204(r14),r1
          % processing RelOp: t41 := result > 2
          lw r5,-172(r14)
          lw r4,-1204(r14)
          cgt r2,r5,r4
          sw -1208(r14),r2
          % processing: if(t41)
          lw r3,-1208(r14)
          bz r3,else0
          % processing: t42 := 4
          addi r2,r0,4
          sw -1212(r14),r2
          % processing: t43 := read_test+4
          lw r4,-584(r14)
          lw r5,-1212(r14)
          add  r1,r4,r5
          sw -1216(r14),r1
          % processing: put(t43)
          lw r1,-1216(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-1260
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-1260
          j endif0
else0
          % processing: t44 := 2000
          addi r5,r0,2000
          sw -1220(r14),r5
          % processing: put(t44)
          lw r5,-1220(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-1260
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-1260
endif0
          % processing: t45 := 2
          addi r1,r0,2
          sw -1224(r14),r1
          % processing RelOp: t46 := result < 2
          lw r5,-172(r14)
          lw r4,-1224(r14)
          clt r2,r5,r4
          sw -1228(r14),r2
          % processing: if(t46)
          lw r3,-1228(r14)
          bz r3,else1
          % processing: t47 := 999
          addi r2,r0,999
          sw -1232(r14),r2
          % processing: put(t47)
          lw r2,-1232(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r2
          % link buffer to stack
          addi r2,r0, buf
          sw -12(r14),r2
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r4,r0,space
          addi r14,r14,-1260
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-1260
          j endif1
else1
          % processing: t48 := 5
          addi r4,r0,5
          sw -1236(r14),r4
          % processing: t49 := read_test+5
          lw r2,-584(r14)
          lw r5,-1236(r14)
          add  r1,r2,r5
          sw -1240(r14),r1
          % processing: put(t49)
          lw r1,-1240(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-1260
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-1260
endif1
          % processing: t50 := 9
          addi r3,r0,9
          sw -1244(r14),r3
          % processing: AssignStatNode(counter=9;)
          % processing: counter := t50
          lw r5,-1244(r14)
          sw -580(r14),r5
          % processing: while(t52)
          j gowhile2
gowhile2
          % processing: t51 := 11
          addi r5,r0,11
          sw -1248(r14),r5
          % processing RelOp: t52 := counter < 11
          lw r3,-580(r14)
          lw r2,-1248(r14)
          clt r4,r3,r2
          sw -1252(r14),r4
          lw r1,-1252(r14)
          bz r1,endwhile2
          % processing: put(counter)
          lw r4,-580(r14)
          % put value on stack
          addi r14,r14,-1260
          sw -8(r14),r4
          % link buffer to stack
          addi r4,r0, buf
          sw -12(r14),r4
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-1260
          % put value on stack
          addi r2,r0,space
          addi r14,r14,-1260
          sw -8(r14),r2
          jl r15, putstr
          subi r14,r14,-1260
          % processing: t53 := 1
          addi r2,r0,1
          sw -1256(r14),r2
          % processing: t54 := counter+1
          lw r4,-580(r14)
          lw r3,-1256(r14)
          add  r5,r4,r3
          sw -1260(r14),r5
          % processing: AssignStatNode(counter=counter+1;)
          % processing: counter := t54
          lw r3,-1260(r14)
          sw -580(r14),r3
          j gowhile2
endwhile2
          % processing: function call to freefunc(counter) 
          lw r1,-580(r14)
          sw -1268(r14),r1
          addi r14,r14,-1260
          jl r15,freefunc2
          subi r14,r14,-1260
          lw r1,-1260(r14)
          sw -1260(r14),r1
          hlt

space db     " ", 0
input db "input:", 0
          % buffer space used for console output
buf       res 20

