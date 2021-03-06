/* CRNToolkit, Copyright (c) 2010-2016 Jost Neigenfind  <jostie@gmx.de>,
 * Sergio Grimbs, Zoran Nikoloski
 * 
 * A Java toolkit for Chemical Reaction Networks
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package system.parsers.cna;

import crnt.*;
import system.parsers.*;

import java.io.*;

public class CNAParser extends Parser{
	public CNAParser(){
	}
	
	public ReactionNetwork parse(File file) throws Exception{
		return this.parse(file.getAbsolutePath());
	}
	
	public ReactionNetwork parse(String file_name) throws Exception{
		ReactionNetwork reaction_network = new ReactionNetwork();
		BufferedReader br = new BufferedReader(new FileReader(new File(file_name)));
		String line = "";
		while ((line = br.readLine()) != null)
			if (line.charAt(0) != '#'){
				Reaction[] ret = this.parseString(line);
				reaction_network.addReaction(ret[0]);
				if (ret[1] != null)
					reaction_network.addReaction(ret[1]);
			}
		
		br.close();
		return reaction_network;
	}
	
	public Reaction[] parseString(String line) throws Exception{	
		Complex substrate = new Complex();
		Complex product = new Complex();
		
		line = line.replaceAll("\t", " ");
		while (!this.test(line))
			line = line.replaceAll("  ", " ");
		
		String foo = "";
		if (line.indexOf("|") != -1)
			foo = line.substring(line.indexOf("|"), line.length());
		String[] cells = foo.split(" ");
		
		boolean backward = false;
		if ((new Integer(cells[2])) != 0)
			backward = true;
		
		boolean forward = false;
		if ((new Integer(cells[3])) != 0)
			forward = true;
		
		String line_ = "";
		if (line.indexOf("|") != -1)
			line_ = line.substring(0, line.indexOf("|"));
		line_ = line_.replaceAll("[+]", "");
		while (!this.test(line_))
			line_ = line_.replaceAll("  ", " ");

		cells = line_.split(" ");
		String name = cells[0];
		boolean p = false;
		for (int i = 1; i < cells.length; i++){
			Integer z = null;
			if (this.testIfNumber(cells[i]))
				z = new Integer(cells[i]);

			p = p || cells[i].equals("=");
			
			if (!cells[i].equals("=") && z != null && !p){
				Species species = new Species(Species.ID_PREFIX + cells[i + 1], cells[i + 1], "default");
				for (int j = 0; j < z; j++)
					substrate.getObject().add(species);
				i++;
			}
			
			if (!cells[i].equals("=") && z != null && p){
				Species species = new Species(Species.ID_PREFIX + cells[i + 1], cells[i + 1], "default");	// set compartment to default
				for (int j = 0; j < z; j++)
					product.getObject().add(species);
				
				i++;
			}
		}
		
		Reaction[] ret = new Reaction[2];
		if (forward && !backward)
			ret[0] = new Reaction(Reaction.ID_PREFIX + name, name, substrate, product);
		else if (!forward && backward)
			ret[0] = new Reaction(Reaction.ID_PREFIX + name, name, product, substrate);
		else if (forward && backward){
			ret[0] = new Reaction(Reaction.ID_PREFIX + name, name, substrate, product);
			ret[1] = new Reaction(Reaction.ID_PREFIX + name + "_back", name + " (back)", product, substrate);
		}
		
		return ret;	
	}
	
	private boolean test(String react){
		return react.indexOf("  ") == -1;
	}	
}
