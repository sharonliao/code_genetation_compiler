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
          entry
          addi r14,r0,topaddr
          % processing: t4 := 1
          addi r4,r0,1
          sw -548(r14),r4
          % processing: AssignStatNode(polynomial1.b=1;)
          % processing:  := t4
          lw r5,-548(r14)
          sw -4(r14),r5
          % processing: t5 := 1
          addi r3,r0,1
          sw -552(r14),r3
          % processing: AssignStatNode(polynomial2.d=1;)
          % processing:  := t5
          lw r5,-552(r14)
          sw -16(r14),r5
          % processing: t6 := 1
          addi r4,r0,1
          sw -556(r14),r4
          % processing: AssignStatNode(polynomial2.b=1;)
          % processing:  := t6
          lw r5,-556(r14)
          sw -12(r14),r5
          % processing: put()
          lw r3,-4(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r3
          % link buffer to stack
          addi r3,r0, buf
          sw -12(r14),r3
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-712
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-712
          % processing: t7 := 2
          addi r5,r0,2
          sw -560(r14),r5
          % processing: AssignStatNode(linear1.polynomial_obj.b=2;)
          % processing:  := t7
          lw r3,-560(r14)
          sw -28(r14),r3
          % processing: AssignStatNode(b=linear1.polynomial_obj.b;)
          % processing: b := 
          lw r3,-28(r14)
          sw -40(r14),r3
          % processing: put(b)
          lw r5,-40(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r3,r0,space
          addi r14,r14,-712
          sw -8(r14),r3
          jl r15, putstr
          subi r14,r14,-712
          % processing: t8 := polynomial1.b+polynomial2.b
          lw r5,-4(r14)
          lw r4,-12(r14)
          add  r1,r5,r4
          sw -564(r14),r1
          % processing: AssignStatNode(c=polynomial1.b+polynomial2.b;)
          % processing: c := t8
          lw r4,-564(r14)
          sw -44(r14),r4
          % processing: function call to polynomial1.evaluate(c) 
          lw r5,-44(r14)
          sw -724(r14),r5
          addi r14,r14,-712
          jl r15,evaluate0
          subi r14,r14,-712
          lw r5,-712(r14)
          sw -568(r14),r5
          % processing: AssignStatNode(result=polynomial1.evaluate(c);)
          % processing: result := t9
          lw r4,-568(r14)
          sw -52(r14),r4
          % processing: put(result)
          lw r1,-52(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r4,r0,space
          addi r14,r14,-712
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-712
          % processing: t12 := 4
          addi r4,r0,4
          sw -580(r14),r4
          % processing: AssignStatNode(array[1][1]=4;)
          % processing:  := t12
          lw r1,-580(r14)
          sw -4456(r14),r1
          % processing: put()
          lw r5,-4456(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-712
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-712
          % processing: t16 := 5
          addi r1,r0,5
          sw -596(r14),r1
          % processing: AssignStatNode(array_object[2]=5;)
          % processing:  := t16
          lw r5,-596(r14)
          sw -628(r14),r5
          % processing: put()
          lw r4,-628(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r4
          % link buffer to stack
          addi r4,r0, buf
          sw -12(r14),r4
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-712
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-712
          % processing: t18 := 4
          addi r5,r0,4
          sw -604(r14),r5
          % processing: t19 := 3
          addi r5,r0,3
          sw -608(r14),r5
          % processing: t20 := 4*3
          lw r4,-604(r14)
          lw r1,-608(r14)
          mul  r3,r4,r1
          sw -612(r14),r3
          % processing: t21 := 4
          addi r3,r0,4
          sw -616(r14),r3
          % processing: t22 := 100
          addi r3,r0,100
          sw -620(r14),r3
          % processing: t23 := 4+100
          lw r1,-616(r14)
          lw r4,-620(r14)
          add  r5,r1,r4
          sw -624(r14),r5
          % processing: t24 := 1
          addi r5,r0,1
          sw -628(r14),r5
          % processing: t25 := 1
          addi r5,r0,1
          sw -632(r14),r5
          % processing: t26 := 1+1
          lw r4,-628(r14)
          lw r1,-632(r14)
          add  r3,r4,r1
          sw -636(r14),r3
          % processing: t27 := 4+100/1+1
          lw r1,-624(r14)
          lw r4,-636(r14)
          div  r5,r1,r4
          sw -640(r14),r5
          % processing: t28 := 4*3+4+100/1+1
          lw r4,-612(r14)
          lw r1,-640(r14)
          add  r3,r4,r1
          sw -644(r14),r3
          % processing: t29 := 58
          addi r3,r0,58
          sw -648(r14),r3
          % processing: t30 := 4*3+4+100/1+1-58
          lw r1,-644(r14)
          lw r4,-648(r14)
          sub  r5,r1,r4
          sw -652(r14),r5
          % processing: AssignStatNode(express_test=4*3+4+100/1+1-58;)
          % processing: express_test := t30
          lw r4,-652(r14)
          sw -56(r14),r4
          % processing: put(express_test)
          lw r1,-56(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r4,r0,space
          addi r14,r14,-712
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r4,r0,input
          addi r14,r14,-712
          sw -8(r14),r4
          jl r15, putstr
          subi r14,r14,-712
          % processing: read(read_test)
          addi r4, r0, buf
          sw -8(r14),r4
          jl r15,getstr
          jl r15,strint
          sw -464(r14), r13
          % processing: t31 := 2
          addi r1,r0,2
          sw -656(r14),r1
          % processing RelOp: t32 := result > 2
          lw r5,-52(r14)
          lw r3,-656(r14)
          cgt r2,r5,r3
          sw -660(r14),r2
          % processing: if(t32)
          lw r4,-660(r14)
          bz r4,else0
          % processing: t33 := 4
          addi r2,r0,4
          sw -664(r14),r2
          % processing: t34 := read_test+4
          lw r3,-464(r14)
          lw r5,-664(r14)
          add  r1,r3,r5
          sw -668(r14),r1
          % processing: put(t34)
          lw r1,-668(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-712
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-712
          j endif0
else0
          % processing: t35 := 2000
          addi r5,r0,2000
          sw -672(r14),r5
          % processing: put(t35)
          lw r5,-672(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r5
          % link buffer to stack
          addi r5,r0, buf
          sw -12(r14),r5
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r1,r0,space
          addi r14,r14,-712
          sw -8(r14),r1
          jl r15, putstr
          subi r14,r14,-712
endif0
          % processing: t36 := 2
          addi r1,r0,2
          sw -676(r14),r1
          % processing RelOp: t37 := result < 2
          lw r5,-52(r14)
          lw r3,-676(r14)
          clt r2,r5,r3
          sw -680(r14),r2
          % processing: if(t37)
          lw r4,-680(r14)
          bz r4,else1
          % processing: t38 := 999
          addi r2,r0,999
          sw -684(r14),r2
          % processing: put(t38)
          lw r2,-684(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r2
          % link buffer to stack
          addi r2,r0, buf
          sw -12(r14),r2
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r3,r0,space
          addi r14,r14,-712
          sw -8(r14),r3
          jl r15, putstr
          subi r14,r14,-712
          j endif1
else1
          % processing: t39 := 5
          addi r3,r0,5
          sw -688(r14),r3
          % processing: t40 := read_test+5
          lw r2,-464(r14)
          lw r5,-688(r14)
          add  r1,r2,r5
          sw -692(r14),r1
          % processing: put(t40)
          lw r1,-692(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r1
          % link buffer to stack
          addi r1,r0, buf
          sw -12(r14),r1
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r5,r0,space
          addi r14,r14,-712
          sw -8(r14),r5
          jl r15, putstr
          subi r14,r14,-712
endif1
          % processing: t41 := 9
          addi r4,r0,9
          sw -696(r14),r4
          % processing: AssignStatNode(counter=9;)
          % processing: counter := t41
          lw r5,-696(r14)
          sw -460(r14),r5
          % processing: while(t43)
          j gowhile2
gowhile2
          % processing: t42 := 11
          addi r5,r0,11
          sw -700(r14),r5
          % processing RelOp: t43 := counter < 11
          lw r4,-460(r14)
          lw r2,-700(r14)
          clt r3,r4,r2
          sw -704(r14),r3
          lw r1,-704(r14)
          bz r1,endwhile2
          % processing: put(counter)
          lw r3,-460(r14)
          % put value on stack
          addi r14,r14,-712
          sw -8(r14),r3
          % link buffer to stack
          addi r3,r0, buf
          sw -12(r14),r3
          % convert int to string for output
          jl r15, intstr
          sw -8(r14),r13
          % output to console
          jl r15, putstr
          subi r14,r14,-712
          % put value on stack
          addi r2,r0,space
          addi r14,r14,-712
          sw -8(r14),r2
          jl r15, putstr
          subi r14,r14,-712
          % processing: t44 := 1
          addi r2,r0,1
          sw -708(r14),r2
          % processing: t45 := counter+1
          lw r3,-460(r14)
          lw r4,-708(r14)
          add  r5,r3,r4
          sw -712(r14),r5
          % processing: AssignStatNode(counter=counter+1;)
          % processing: counter := t45
          lw r4,-712(r14)
          sw -460(r14),r4
          j gowhile2
endwhile2
          hlt

space db     " ", 0
input db "input:", 0
          % buffer space used for console output
buf       res 20

