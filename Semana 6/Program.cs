using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using System.Text.RegularExpressions;

namespace Assembler
{
    class Program
    {
        //C-Instruction
        private static Dictionary<String,String> dest = new Dictionary<string, string>();
        private static Dictionary<String,String> jump = new Dictionary<string, string>();
        private static Dictionary<String,String> comp = new Dictionary<string, string>();

        //A-Instruction
        private static Dictionary<String, String> symbols = new Dictionary<string, string>();
        private static Dictionary<String, String> labels = new Dictionary<string, string>();
        private static Dictionary<String, String> variables = new Dictionary<string, string>();

        private static int control = 16;
        private static List<string> foundLabels = new List<string>();

        static void Main(string[] args)
        {
            Tabelas();
            String input = Console.ReadLine();
            String path = "C:\\Users\\igors\\Documents\\" + input;

            Console.WriteLine("Abrindo arquivo " + path);

            //Checa se arquivo existe
            FileInfo info = new FileInfo(@path);
            if (info.Exists)
            {
                Parser(info.FullName);
            }

            Console.Read();

        }

        static void Parser(String filepath)
        {
            StreamReader streamReader = new StreamReader(@filepath);
            String line;
            List<string> buffer = new List<string>();
            int count = 0;

            while ((line = streamReader.ReadLine()) != null)
            {
                if (line.Contains("//") || line.Contains("\n") || line.Contains(" "))
                {
                    if (line.Contains("//")) { line = line.Remove(line.IndexOf('/')); }
                    line = line.Replace("\n", "").Replace("\t","").Replace("\r","");
                    line = line.Replace(" ", "");
                }

                if(line != "")
                {
                    
                    if (line.Contains("(") && line.Contains(")"))
                    {
                        labels.Add(line.Replace("(", "").Replace(")", ""), ConvertDecToBin(count.ToString()) );
                    }
                    else if (line.StartsWith("@")) //A-Instruction
                    {
                        line = AIstrParser(line);
                        buffer.Add(line);
                    }
                    else //C-Instruction  dest=comp;jump
                    {
                        line = CIstrParser(line);
                        buffer.Add(line);
                    }

                    count++;
                }

            }

            while(foundLabels.Count != 0)
            {
                string aux = foundLabels.First();
                labels.TryGetValue(aux, out string value);

                int i;

                while ((i = buffer.IndexOf(aux)) != -1)
                {
                    buffer.RemoveAt(i);
                    buffer.Insert(i, value);
                }
                foundLabels.Remove(aux);
            }
            filepath = filepath.Replace(".asm", ".hack");

            StreamWriter streamWriter = new StreamWriter(@filepath);
            
            while (buffer.Count != 0) {
                streamWriter.WriteLine(buffer.First());
                Console.WriteLine(buffer.First());
                buffer.Remove(buffer.First());
            }
            streamWriter.Close();
            streamReader.Close();

        }


        private static string AIstrParser(string line)
        {
            string aux = line.Replace("@","");

           if(int.TryParse(aux, out int res))
            {
                aux = ConvertDecToBin(aux); 
            }
            else
            {
               bool exists = symbols.TryGetValue(aux, out string output); //pre defined symbols
                
                if (exists) {
                    aux = ConvertDecToBin(output);
                }
                else
                {
                   if( new Regex(aux).Matches("([A-Z])+").ToString() != "")
                    {
                        //Label
                        if(labels.TryGetValue(aux, out string value)) { aux = value; } else
                        {
                            foundLabels.Add(aux);
                        }
                        
                    }
                    else
                    {
                        //Variavel
                        if(variables.TryGetValue(aux, out string value))
                        {
                            aux = value;
                        }
                        else
                        {
                            variables.Add(aux, ConvertDecToBin(control.ToString()));
                            aux = ConvertDecToBin(control.ToString());
                            control++;
                        }
                       
                    }
                }
            }

            return aux;
        }

        private static string CIstrParser(string line)
        {
            //dest=comp;jump
            //111comp[a c1 c2 c3 c4 c5 c6]dest[d1 d2 d3]jump[j1 j2 j3]]
            String[] aux;
            String cmp = "";
            String dst = "";
            String jmp = "";
            if (line.Contains("=") && line.Contains(";"))
            {
                aux = line.Split('=');
                dst = aux[0];
                cmp = aux[1].Split(';')[0];
                jmp = aux[1].Split(';')[1];
            }
            else
            {
                if (line.Contains("="))
                {
                    aux = line.Split('=');
                    dst = aux[0];
                    cmp = aux[1];
                    jmp = "null";
                }
                if (line.Contains(";"))
                {
                    aux = line.Split(';');
                    dst = "null";
                    cmp = aux[0];
                    jmp = aux[1];
                }
                if (!line.Contains("=") && !line.Contains(";"))
                {
                    //aux = line.Split(';');
                    dst = "null";
                    cmp = line;
                    jmp = "null";
                }
            }
            
            dest.TryGetValue(key: dst.ToString(), value: out string destResult);
            jump.TryGetValue(key: jmp.ToString(), value: out string jumpResult);
            comp.TryGetValue(key: cmp.ToString(), value: out string compResult);

            return "111"+compResult+destResult+jumpResult;

        }

        static void Tabelas()
        {
            try
            {
                dest.Add("null", "000");
                dest.Add("M", "001");
                dest.Add("D", "010");
                dest.Add("MD", "011");
                dest.Add("A", "100");
                dest.Add("AM", "101");
                dest.Add("AD", "110");
                dest.Add("AMD", "111");

                jump.Add("null", "000");
                jump.Add("JGT", "001");
                jump.Add("JEQ", "010");
                jump.Add("JGE", "011");
                jump.Add("JLT", "100");
                jump.Add("JNE", "101");
                jump.Add("JLE", "110");
                jump.Add("JMP", "111");

                comp.Add("0", "0101010");
                comp.Add("1", "0111111");
                comp.Add("-1", "0111010");
                comp.Add("D", "0001100");
                comp.Add("A", "0110000");
                comp.Add("!D", "0001101");
                comp.Add("!A", "0110001");
                comp.Add("-D", "0001111");
                comp.Add("-A", "0110011");
                comp.Add("D+1", "0101111");
                comp.Add("A+1", "0111011");
                comp.Add("D-1", "0100111");
                comp.Add("A-1", "0111001");
                comp.Add("D+A", "0000010");
                comp.Add("D-A", "0010011");
                comp.Add("A-D", "0000111");
                comp.Add("D&A", "0000000");
                comp.Add("D|A", "0010101");

                comp.Add("M", "1110000");
                comp.Add("!M", "1110001");
                comp.Add("-M", "1110011");
                comp.Add("M+1", "1111011");
                comp.Add("M-1", "1111001");
                comp.Add("D+M", "1000010");
                comp.Add("D-M", "1010011");
                comp.Add("M-D", "1000111");
                comp.Add("D&M", "1000000");
                comp.Add("D|M", "1010101");

                symbols.Add("SP", "0");
                symbols.Add("ARG", "2");
                symbols.Add("LCL", "1");
                symbols.Add("THIS", "3");
                symbols.Add("THAT", "4");
                symbols.Add("R0", "0");
                symbols.Add("R1", "1");
                symbols.Add("R2", "2");
                symbols.Add("R3", "3");
                symbols.Add("R4", "4");
                symbols.Add("R5", "5");
                symbols.Add("R6", "6");
                symbols.Add("R7", "7");
                symbols.Add("R8", "8");
                symbols.Add("R9", "9");
                symbols.Add("R10", "10");
                symbols.Add("R11", "11");
                symbols.Add("R12", "12");
                symbols.Add("R13", "13");
                symbols.Add("R14", "14");
                symbols.Add("R15", "15");
                symbols.Add("SCREEN", "16384");
                symbols.Add("KBD", "24576");

            }
            catch 
            {
                throw new Exception("Erro ao carregar tabelas");
            }
            
        }

        public static string ConvertDecToBin(string input)
        {
            input = Convert.ToString(Int16.Parse(input), 2);
            return new string('0', 16 - input.Length) + input;
        }
    }
}
